package org.praekelt.restforms.core.services;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryModel;
import org.javarosa.model.xform.XFormSerializingVisitor;
import org.javarosa.xform.util.XFormAnswerDataParser;
import org.javarosa.xform.util.XFormUtils;
import org.praekelt.restforms.core.exceptions.RosaException;

/**
 * 
 * 
 * @author ant cosentino
 */
public final class RosaFactory implements Serializable {
    private transient FormDef form;
    private transient FormEntryController controller;
    private transient FormEntryModel model;
    private transient FormIndex[] questionIndicies;
    private static final long serialVersionUID = 1L;
    private String[] questionTexts;
    private String xmlForm;
    private int total, completed;
    
    /**
     * steps the instance's formentrycontroller
     * to the given formindex position.
     * 
     * @param position 
     */
    private void setCursor(FormIndex position) {
        controller.jumpToIndex(position);
    }
    
    /**
     * this method is only ever used post-initialisation
     * or post-unserialisation. its responsibility is to
     * populate the transient fields of the instance with
     * the data needed to process a form.
     */
    private void setQuestionMetadata(boolean fresh) {
        int event, current = 0;
        
        total = model.getNumQuestions();
        questionIndicies = new FormIndex[total];
        questionTexts = fresh ? new String[total] : questionTexts;
        
        while ((event = controller.stepToNextEvent()) != FormEntryController.EVENT_END_OF_FORM) {

            if (event == FormEntryController.EVENT_QUESTION) {
                questionTexts[current] = fresh ? model.getQuestionPrompt().getQuestionText() : questionTexts[current];
                questionIndicies[current++] = model.getFormIndex();
            }
        }
    }
    
    private IAnswerData castAnswer(Object answer) {
        String response = answer.toString();
        int type = model.getQuestionPrompt().getDataType();
        QuestionDef def = model.getQuestionPrompt().getQuestion();
        return XFormAnswerDataParser.getAnswerData(response, type, def);
    }
    
    public static RosaFactory rebuild(byte[] buffer) throws RosaException {
        
        if (buffer != null && buffer.length > 0) {
            
            try {
                ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(buffer));
                RosaFactory rf = (RosaFactory) ois.readObject();
                ois.close();
                
                if (rf.setUp()) {
                    return rf;
                }
            } catch (IOException e) {
                throw new RosaException(e);
            } catch (ClassNotFoundException e) {
                throw new RosaException(e);
            }
        }
        return null;
    }
    
    public static byte[] persist(RosaFactory r) throws RosaException {
        
        if (r != null) {
            
            try {
                ByteArrayOutputStream bao = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(bao);
                oos.writeObject(r);
                byte[] buffer = bao.toByteArray();
                oos.close();
                return buffer;
            } catch (IOException e) {
                throw new RosaException(e);
            }
        }
        return null;
    }
    
    /**
     * this method reads in a given xform, using a javarosa
     * utility class, which produces a formdef instance - on 
     * which the other instance fields of the class are dependent.
     * the provided xform is also assigned to an instance field
     * to make serialisation and unserialisation easier.
     * if this method is used after unserialising, it sets the
     * formentrycontroller's position to the next unanswered question.
     * 
     * @param xmlForm xform string
     * @param fresh is this a new instance, or is it being unserialised?
     * @return boolean whether or not the initialisation process was successful
     */
    public boolean setUp(String xmlForm, boolean fresh) {
        
        if (xmlForm != null && !xmlForm.isEmpty()) {
            this.xmlForm = xmlForm;
            form = XFormUtils.getFormFromInputStream(new ByteArrayInputStream(this.xmlForm.getBytes()));
            model = new FormEntryModel(form);
            controller = new FormEntryController(model);
            setQuestionMetadata(fresh);

            if (!fresh) {
                setCursor(questionIndicies[completed]);
            } else {
                completed = 0;
            }
            return true;
        }
        return false;
    }
    
    public boolean setUp(String xmlForm) {
        return this.setUp(xmlForm, true);
    }
    
    public boolean setUp() {
        return this.setUp(this.xmlForm, false);
    }
    
    public String[] getQuestionTexts() {
        return questionTexts;
    }
    
    public String getQuestion(int question) {
        return question > -1 && question < total ? questionTexts[question] : null;
    }
    
    public int getCompleted() {
        return completed;
    }
    
    public int getTotal() {
        return total;
    }
    
    /**
     * the initial conditional block of this method evaluates
     * which question is to be answered. provided the given
     * question argument is within reasonable bounds, any question
     * of a form may be attempted and reattempted. if the question 
     * argument is simply -1, the question flow will be regular 
     * (ie, sequential and repeating any question that is given 
     * an invalid answer)
     * 
     * @param answer
     * @param question integer pointer to the instance's array of form indices
     * @return boolean whether or not the answer was committed to the xform
     * @throws RosaException thrown if the answerdata object returned is null 
     * (indicating a numberformatexception, for example) or if the 
     * formentrycontroller's answer constants are anything but ANSWER_OK
     */
    public boolean answerQuestion(Object answer, int question) throws RosaException {
        
        if (question == -1 && completed < total) {
            setCursor(questionIndicies[completed]);
        } else if (question > -1 && question < total) {
            setCursor(questionIndicies[question]);
        } else {
            return false;
        }

        if (model.getEvent() == FormEntryController.EVENT_QUESTION) {
            IAnswerData a = castAnswer(answer);

            if (a != null) {

                switch (controller.answerQuestion(a)) {
                    case FormEntryController.ANSWER_CONSTRAINT_VIOLATED:
                        throw new RosaException("Answer constraint violated");
                    case FormEntryController.ANSWER_REQUIRED_BUT_EMPTY:
                        throw new RosaException("Answer required but found empty");
                    default:
                        completed++;
                        return true;
                }
            } else {
                throw new RosaException("Got NULL from RosaFactory#castAnswer()");
            }
        }
        return false;
    }
    
    public boolean answerQuestion(Object answer) throws RosaException {
        return answerQuestion(answer, -1);
    }
    
    public String getCompletedXForm() throws RosaException {
        XFormSerializingVisitor x;
        
        if (completed == total) {
            x = new XFormSerializingVisitor();
        
            try {
                return new String(x.serializeInstance(form.getInstance()));
            } catch (IOException e) {
                throw new RosaException(e.getMessage());
            }
        }
        return null;
    }
}
