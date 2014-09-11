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
    private transient String[] questionTexts;
    private transient int total, questionEvents[], questionTypes[];
    private static final long serialVersionUID = 1L;
    private String xmlForm;
    private int completed;
    
    private void setCursor(FormIndex position) {
        controller.jumpToIndex(position);
    }
    
    private void setQuestionMetadata() {
        int event, current = 0;
        
        total = model.getNumQuestions();
        questionTexts = new String[total];
        questionEvents = new int[total];
        questionIndicies = new FormIndex[total];
        questionTypes = new int[total];
                
        while ((event = controller.stepToNextEvent()) != FormEntryController.EVENT_END_OF_FORM) {

            if (event == FormEntryController.EVENT_QUESTION) {
                questionTexts[current] = model.getQuestionPrompt().getQuestionText();
                questionEvents[current] = model.getEvent();
                questionTypes[current] = model.getQuestionPrompt().getDataType();
                questionIndicies[current++] = model.getFormIndex();
            }
        }
    }
    
    private IAnswerData castAnswer(Object answer, int question) {
        String a = answer.toString();
        int i = question != -1 && question <= total ? questionTypes[question] : questionTypes[completed];
        QuestionDef d = model.getQuestionPrompt().getQuestion();
        return XFormAnswerDataParser.getAnswerData(a, i, d);
    }
    
    public static RosaFactory rebuild(byte[] buffer) throws RosaException {
        
        try {
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(buffer));
            RosaFactory rf = (RosaFactory) ois.readObject();
            ois.close();
            return rf;
        } catch (IOException e) {
            throw new RosaException(e);
        } catch (ClassNotFoundException e) {
            throw new RosaException(e);
        }
    }
    
    public static byte[] persist(RosaFactory r) throws RosaException {
        
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
    
    public boolean setUp(String xmlForm, boolean fresh) {
        
        if (xmlForm != null && !xmlForm.isEmpty()) {
            this.xmlForm = xmlForm;
            form = XFormUtils.getFormFromInputStream(new ByteArrayInputStream(this.xmlForm.getBytes()));
            model = new FormEntryModel(form);
            controller = new FormEntryController(model);
            setQuestionMetadata();

            if (!fresh) {
                setCursor(questionIndicies[completed]);
            } else {
                completed = 0;
            }
            return true;
        }
        return false;
    }
    
    public boolean setUp() {
        return this.setUp(this.xmlForm, false);
    }
    
    public String[] getQuestionTexts() {
        return questionTexts;
    }
    
    public String getQuestion(int index) {
        return index >= 0 && index <= questionTexts.length - 1 ? questionTexts[index] : null;
    }
    
    public int getCompleted() {
        return completed;
    }
    
    public int getTotal() {
        return total;
    }
    
    public boolean answerQuestion(Object answer, int question) throws RosaException {
        
        if (question == -1 && completed < total) {
            controller.jumpToIndex(questionIndicies[completed]);
        } else if (question >= 0 && question <= total) {
            controller.jumpToIndex(questionIndicies[question]);
        } else {
            return false;
        }

        if (model.getEvent() == FormEntryController.EVENT_QUESTION) {
            IAnswerData a = castAnswer(answer, question);

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
}
