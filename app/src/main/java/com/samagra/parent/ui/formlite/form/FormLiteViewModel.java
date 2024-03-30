package com.samagra.parent.ui.formlite.form;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.samagra.parent.ui.formlite.model.InputField;
import com.samagra.parent.ui.formlite.model.StudentDetailsFormData;
import java.util.HashMap;
import java.util.Map;
import io.reactivex.disposables.Disposable;


public class FormLiteViewModel extends ViewModel {

    private Disposable disposable;
    public MutableLiveData<Boolean> sendOTPLiveData = new MutableLiveData<>();
    private MutableLiveData<Boolean> verifyOTPLiveData = new MutableLiveData<>();
    private Map<String, Object> dataStore;
    private MutableLiveData<String> verifiedSLC = new MutableLiveData<>();
    private MutableLiveData<String> generatedSLC = new MutableLiveData<>();
    private StudentDetailsFormData formData;
    private MutableLiveData<String> noteText = new MutableLiveData<>();

    private final MutableLiveData<Boolean> showLoaderLiveData = new MutableLiveData<>();

    public FormLiteViewModel() {
        initService();
    }


    public void addToDataStore(String key, Object value) {
        dataStore.put(key, value);
    }

    public Map<String, Object> getDataStore() {
        return dataStore;
    }

    public void initService() {
        dataStore = new HashMap<>();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (disposable != null) {
            disposable.dispose();
        }
    }

    public Object getValueFromStore(String key) {
        return dataStore.get(key);
    }

    public MutableLiveData<Boolean> getShowLoaderLiveData() {
        return showLoaderLiveData;
    }

    public StudentDetailsFormData getFormData() {
        return formData;
    }

    public void setFormData(StudentDetailsFormData formData) {
        this.formData = formData;
    }

    public InputField getAdditionalField(String key) {
        if (formData != null) {
            for (InputField field : formData.getAdditionalFields()) {
                if (field.getKey().equalsIgnoreCase(key)) {
                    return field;
                }
            }
        }
        return null;
    }
}
