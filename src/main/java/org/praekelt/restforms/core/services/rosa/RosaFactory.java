package org.praekelt.restforms.core.services.rosa;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
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
    private transient FormEntryController controller;
    private transient FormEntryModel model;
    private transient FormDef form;
    private transient FormIndex[] questionIndices;
    private transient static final Logger logger = Logger.getLogger("RosaFactory");
    private static final long serialVersionUID = 1L;
    private String xmlForm, questionTexts[];
    private int total, completed;
    
    /**
     * steps the instance's formentrycontroller
     * to the given formindex position.
     * 
     * @param position 
     */
    private int setCursor(int question) throws RosaException {
        
        if (question == -1 && completed < total) {
            return controller.jumpToIndex(questionIndices[completed]);
        } else if (question > -1 && question < total) {
            return controller.jumpToIndex(questionIndices[question]);
        } else {
            logger.log(Level.ERROR, "RosaFactory#setCursor(): given argument, " + question + " , is out of bounds.");
            throw new RosaException("The question number was out of bounds.");
        }
    }
    
    /**
     * this method is only ever used post-initialisation
     * or post-unserialisation. its responsibility is to
     * populate the transient fields of the instance with
     * the data needed to process a form.
     * 
     * @param fresh
     * @throws RosaException thrown if model returned null, which indicates a malformed xml document.
     */
    private void setQuestionMetadata(boolean fresh) {
        int event, 
            formEnd = FormEntryController.EVENT_END_OF_FORM,
            i = 0;
        
        total = model.getNumQuestions();
        questionIndices = new FormIndex[total];
        questionTexts = fresh ? new String[total] : questionTexts;

        while ((event = controller.stepToNextEvent()) != formEnd) {

            if (event == FormEntryController.EVENT_QUESTION) {
                questionTexts[i] = fresh ? model.getQuestionPrompt().getQuestionText() : questionTexts[i];
                questionIndices[i++] = model.getFormIndex();
            }
        }
    }
    
    private IAnswerData castAnswer(String answer) {
        int type = model.getQuestionPrompt().getDataType();
        QuestionDef def = model.getQuestionPrompt().getQuestion();
        return XFormAnswerDataParser.getAnswerData(answer, type, def);
    }
    
    private byte[] serialiseXForm() throws RosaException {
        XFormSerializingVisitor x;
        
        if (completed == total) {
            x = new XFormSerializingVisitor();
        
            try {
                return x.serializeInstance(form.getInstance());
            } catch (IOException e) {
                logger.log(
                    Level.ERROR,
                    "RosaFactory#serialiseXForm(): unable to parse and generate a model/instance XML document from the given argument, " + form.getInstance().toString() + "." + 
                    " IOException trace: " + e.getMessage()
                );
                throw new RosaException("Unable to create a model/instance XML document.");
            }
        }
        return null;
    }
    
    private void checkRelevanceOfRemaining() {
        int question = completed;
        
        while (question < total) {
            
            if (model.isIndexRelevant(questionIndices[question++])) {
                break;
            }
            completed++;
        }
    }
    
    public static RosaFactory rebuild(byte[] buffer) throws RosaException {
        
        if (buffer != null && buffer.length > 0) {
            
            try {
                ByteArrayInputStream bai = new ByteArrayInputStream(buffer);
                ObjectInputStream ois = new ObjectInputStream(bai);
                RosaFactory rf = (RosaFactory) ois.readObject();
                ois.close();
                
                if (rf.setUp()) {
                    return rf;
                }
            } catch (IOException e) {
                logger.log(
                    Level.ERROR,
                    "RosaFactory#rebuild(): an IOException occurred while attempting to unserialise an instance. Trace: " + e.getMessage()
                );
                throw new RosaException("Unable to unserialise the given xForm for processing.");
            } catch (ClassNotFoundException e) {
                logger.log(
                    Level.ERROR,
                    "RosaFactory#rebuild(): a ClassNotFoundException occurred while attempting to unserialise an instance. Trace: " + e.getMessage()
                );
                throw new RosaException("Unable to unserialise the given xForm for processing.");
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
                logger.log(
                    Level.ERROR,
                    "RosaFactory#rebuild(): an IOException occurred while attempting to serialise an instance. Trace: " + e.getMessage()
                );
                throw new RosaException("Unable to serialise the given xForm instance.");
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
     * @throws org.praekelt.restforms.core.exceptions.RosaException thrown if the xml provided is not valid and/or well-formed
     */
    public boolean setUp(String xmlForm, boolean fresh) throws RosaException {
        
        if (xmlForm != null && !xmlForm.isEmpty()) {
            ByteArrayInputStream bai = new ByteArrayInputStream(xmlForm.getBytes());
            
            try {
                form = XFormUtils.getFormFromInputStream(bai);
                model = new FormEntryModel(form);
                controller = new FormEntryController(model);
                
                setQuestionMetadata(fresh);

                if (!fresh) {

                    if (completed != total) {
                        setCursor(completed);
                    }
                } else {
                    this.xmlForm = xmlForm;
                    completed = 0;
                }
            } catch (RuntimeException e) {
                logger.log(
                    Level.ERROR,
                    "RosaFactory#setUp(): a RuntimeException occurred while attempting to initialise a RosaFactory instance." + 
                    "This may suggest a malformed XML document given as a method argument. Arguments: " + 
                    xmlForm + ", " +
                    true + ", Trace: " + 
                    e.getMessage()
                );
                throw new RosaException("The given XML document was found to be malformed.");
            }
            return true;
        }
        return false;
    }
    
    public boolean setUp(String xmlForm) throws RosaException {
        return this.setUp(xmlForm, true);
    }
    
    public boolean setUp() throws RosaException {
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
     * @return boolean whether or not the question was relevant
     * @throws RosaException thrown if the answerdata object returned is null 
     * (indicating a numberformatexception, for example) or if the 
     * formentrycontroller's answer constants are anything but ANSWER_OK
     */
    public int answerQuestion(String answer, int question) throws RosaException {
        setCursor(question);
        IAnswerData a = castAnswer(answer);

        if (a != null) {

            switch (controller.answerQuestion(a)) {
                case FormEntryController.ANSWER_CONSTRAINT_VIOLATED:
                    throw new RosaException("Answer constraint was violated.");
                case FormEntryController.ANSWER_REQUIRED_BUT_EMPTY:
                    throw new RosaException("Answer was required but found empty.");
                default:
                    completed++;
                    checkRelevanceOfRemaining();
                    return (completed < total) ? completed : -1;
            }
        }
        throw new RosaException("Answer data-type was incorrect.");
    }
    
    public int answerQuestion(String answer) throws RosaException {
        return answerQuestion(answer, -1);
    }
    
    public String getCompletedXForm() throws RosaException {
        byte[] serialised = serialiseXForm();
        return serialised != null ? new String(serialised) : null;
    }
}