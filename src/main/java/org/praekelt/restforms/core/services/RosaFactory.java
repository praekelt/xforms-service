package org.praekelt.restforms.core.services;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.Vector;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.Constants;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.data.BooleanData;
import org.javarosa.core.model.data.DateData;
import org.javarosa.core.model.data.DateTimeData;
import org.javarosa.core.model.data.DecimalData;
import org.javarosa.core.model.data.GeoPointData;
import org.javarosa.core.model.data.IntegerData;
import org.javarosa.core.model.data.LongData;
import org.javarosa.core.model.data.SelectMultiData;
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.data.TimeData;
import org.javarosa.core.model.data.UncastData;
import org.javarosa.core.model.data.helper.Selection;
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
    
    private IAnswerData castAnswer(Object answer) throws ClassCastException {
        
        switch (questionTypes[completed]) {
            case Constants.DATATYPE_INTEGER:
                return new IntegerData((Integer) answer);
            case Constants.DATATYPE_DECIMAL:
                return new DecimalData((Double) answer);
            case Constants.DATATYPE_LONG:
                return new LongData((Long) answer);
            case Constants.DATATYPE_BOOLEAN:
                return new BooleanData((Boolean) answer);
            case Constants.DATATYPE_CHOICE:
                return new SelectOneData((Selection) answer);
            case Constants.DATATYPE_CHOICE_LIST:
                return new SelectMultiData((Vector) answer);
            case Constants.DATATYPE_TEXT:
                return new StringData((String) answer);
            case Constants.DATATYPE_DATE:
                return new DateData((Date) answer);
            case Constants.DATATYPE_TIME:
                return new TimeData((Date) answer);
            case Constants.DATATYPE_DATE_TIME:
                return new DateTimeData((Date) answer);
            case Constants.DATATYPE_GEOPOINT:
                return new GeoPointData((double[]) answer);
            case Constants.DATATYPE_BARCODE:
            case Constants.DATATYPE_BINARY:
            case Constants.DATATYPE_UNSUPPORTED:
            case Constants.DATATYPE_NULL:
            default:
                return new UncastData((String) answer);
        }
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
                
                try {
                    IAnswerData a = castAnswer(answer);

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
                } catch (ClassCastException e) {
                    System.err.println("you entered an incorrectly typed value.");
                }
            }
        }
        return false;
    }
}
