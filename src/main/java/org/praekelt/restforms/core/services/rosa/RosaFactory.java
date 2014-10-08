package org.praekelt.restforms.core.services.rosa;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Vector;
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
 * this service facilitates the processing
 * of xform documents. an instance of this 
 * class represents a single xform document.
 * 
 * @author ant cosentino <ant@io.co.za>
 * @since 2014-10-10
 * @see java.io.Serializable
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
    private Vector<String> answers;
    
    /**
     * steps the instance's formentrycontroller to the 
     * given formindex position.
     * 
     * @param question integer pointer to a desired 
     * form index
     * @return integer the integer event value
     * @throws RosaException if the integer pointer is 
     * out of bounds
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
     * captures the form indices corresponding to questions
     * in the xform and populates other instance fields needed
     * for xform processing.
     * 
     * this method is only ever used post-initialisation
     * or post-unserialisation. its responsibility is to
     * populate the transient fields of the instance with
     * the data needed to process a form.
     * 
     * @param fresh
     */
    private void setXFormMetadata(boolean fresh) throws RosaException {
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
    
    /**
     * casts the answer from string to the correct type
     * expected in the current question.
     * 
     * @param answer string representation of answer to be added to xform
     * @return ianswerdata wrapped typed answer data
     */
    private IAnswerData castAnswer(String answer) {
        int type = model.getQuestionPrompt().getDataType();
        QuestionDef def = model.getQuestionPrompt().getQuestion();
        return XFormAnswerDataParser.getAnswerData(answer, type, def);
    }
    
    /**
     * parses the form model and generates complete
     * instance data
     * 
     * @return byte[] representing the generated model/instance
     * @throws RosaException if an ioexception occurred within
     * the javarosa library
     */
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
    
    /**
     * increments the completed question counter until
     * a relevant question is found
     */
    private void checkRelevanceOfRemaining() {
        int question = completed;
        
        while (question < total) {
            
            if (model.isIndexRelevant(questionIndices[question++])) {
                break;
            }
            completed++;
        }
    }
    
    /**
     * unserialises a rosafactory instance from a byte[]
     * 
     * @param buffer byte[] serialised rosafactory
     * @return rosafactory unserialised instance
     * @throws RosaException if an ioexception occurred during
     * unserialisation or if a classnotfoundexception occurred
     * during unserialisation
     */
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
                    "RosaFactory#rebuild(): an IOException occurred while attempting to unserialise an instance. Message: " + e.getMessage()
                );
                throw new RosaException("Unable to unserialise the given xForm for processing.");
            } catch (ClassNotFoundException e) {
                logger.log(
                    Level.ERROR,
                    "RosaFactory#rebuild(): a ClassNotFoundException occurred while attempting to unserialise an instance. Message: " + e.getMessage() + "."
                );
                throw new RosaException("Unable to unserialise the given XForm for processing.");
            }
        }
        return null;
    }
    
    /**
     * serialises a given rosafactory instance to a byte[]
     * ready for external storage
     * 
     * @param r rosafactory instance to be serialised
     * @return byte[] serialised rosafactory instance
     * @throws RosaException if an ioexception occurred
     * during serialisation
     */
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
                    "RosaFactory#rebuild(): an IOException occurred while attempting to serialise an instance. Message: " + e.getMessage() + "."
                );
                throw new RosaException("Unable to serialise the given XForm instance.");
            }
        }
        return null;
    }
    
    /**
     * initialises instance fields needed for xform processing.
     * 
     * this method reads in a given xform, using a javarosa
     * utility class, which produces a formdef instance - on 
     * which the other instance fields of the class are dependent.
     * the provided xform is also assigned to an instance field
     * to make serialisation and unserialisation easier.
     * if this method is used after unserialising, it sets the
     * formentrycontroller's position to the next unanswered question.
     * 
     * @param xmlForm xform document assumed to be valid and well-formed
     * @param fresh whether this is a new instance or recently unserialised
     * @return boolean whether the method succeeded
     * @throws RosaException if a runtime exception is caught which 
     * suggests a malformed xml document
     */
    public boolean setUp(String xmlForm, boolean fresh) throws RosaException {
        
        if (xmlForm != null && !xmlForm.isEmpty()) {
            ByteArrayInputStream bai = new ByteArrayInputStream(xmlForm.getBytes());
            
            try {
                form = XFormUtils.getFormFromInputStream(bai);
                model = new FormEntryModel(form);
                controller = new FormEntryController(model);
                setXFormMetadata(fresh);
                
                if (!fresh && completed != total) {
                    setCursor(completed);
                } else {
                    this.xmlForm = xmlForm;
                    answers = new Vector<String>(total);
                    completed = 0;
                }
            } catch (RuntimeException e) {
                logger.log(
                    Level.ERROR,
                    "RosaFactory#setUp(): a RuntimeException occurred while attempting to initialise a RosaFactory instance." + 
                    " This may suggest a malformed XML document given as a method argument. Arguments: " + 
                    xmlForm + ", " +
                    true + ", Message: " + 
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
     * commits an answer to the instance's internal form model.
     * 
     * @param answer the actual answer value (to be converted to 
     * the applicable type from within this method)
     * @param bulk answering all questions at once
     * @return integer pointer to the next question string or -1 if form is complete
     * @throws RosaException if the answerdata object returned is null 
     * (indicating a {@link java.lang.NumberFormatException}, for example) or if the 
     * {@link org.javarosa.form.api.FormEntryController}'s answer constants are anything but ANSWER_OK
     * 
     */
    public int processAnswer(String answer, boolean bulk) throws RosaException {
        setCursor(completed);
        IAnswerData a = castAnswer(answer);

        if (a != null) {
            
            if (!bulk) {
                answers.insertElementAt(answer, completed);
            }
            
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
    
    public int processAnswer(String answer) throws RosaException {
        return processAnswer(answer, false);
    }
    
    private void answerAllQuestions() throws RosaException {
        completed = 0;
        
        while (completed < total) {
            processAnswer(answers.get(completed), true);
        }
    }
    
    /**
     * serialises and stringifies the model/instance data of the
     * xform associated with this {@link org.praekelt.restforms.core.services.rosa.RosaFactory} instance.
     * 
     * @return string model/instance data
     * @throws RosaException 
     */
    public String getCompletedXForm() throws RosaException {
        answerAllQuestions();
        byte[] serialised = serialiseXForm();
        return serialised != null ? new String(serialised) : null;
    }
}
