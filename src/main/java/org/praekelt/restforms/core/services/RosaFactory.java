package org.praekelt.restforms.core.services;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.Constants;
import org.javarosa.core.model.FormIndex;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryModel;
import org.javarosa.form.api.FormEntryPrompt;
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
    
    private IAnswerData castAnswer(IAnswerData a, Object answer) {
        
        switch (questionTypes[completed]) {
            case Constants.DATATYPE_UNSUPPORTED:
                break;
            case Constants.DATATYPE_NULL:
                a.setValue(null);
                break;
            case Constants.DATATYPE_INTEGER:
                a.setValue((Integer) answer);
                break;
            case Constants.DATATYPE_DECIMAL:
                a.setValue((Float) answer);
                break;
            case Constants.DATATYPE_LONG:
                a.setValue((Long) answer);
                break;
            case Constants.DATATYPE_BINARY:
                a.setValue(Integer.toBinaryString((Integer) answer));
                break;
            case Constants.DATATYPE_BOOLEAN:
                a.setValue((Boolean) answer);
                break;
            case Constants.DATATYPE_CHOICE:
                break;
            case Constants.DATATYPE_CHOICE_LIST:
                break;
            case Constants.DATATYPE_TEXT:
            case Constants.DATATYPE_DATE:
            case Constants.DATATYPE_TIME:
            case Constants.DATATYPE_DATE_TIME:
            case Constants.DATATYPE_BARCODE:
            case Constants.DATATYPE_GEOPOINT:
            default:
                a.setValue((String) answer);
                break;
        }
        return a;
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
    
    public boolean answerQuestion(Object answer, int question) {
        
        if (completed < total) {
            
            if (question == -1) {
                controller.jumpToIndex(questionIndicies[completed]);
            } else if (question >= 0 && question <= total) {
                controller.jumpToIndex(questionIndicies[question]);
            } else {
                return false;
            }
            
            if (model.getEvent() == FormEntryController.EVENT_QUESTION) {
            
                FormEntryPrompt prompt = model.getQuestionPrompt();
                int controlType = prompt.getControlType();
                IAnswerData a = castAnswer(prompt.getAnswerValue(), answer);

                if (a != null) {

                    switch (controller.answerQuestion(a)) {
                        case FormEntryController.ANSWER_OK:
                            completed++;
                            return true;
                        case FormEntryController.ANSWER_CONSTRAINT_VIOLATED:
                            return false;
                        case FormEntryController.ANSWER_REQUIRED_BUT_EMPTY:
                            return false;
                        default:
                            System.out.println("DEFAULT");
                            return false;
                    }
                }
            }
        }
        return false;
    }
}
