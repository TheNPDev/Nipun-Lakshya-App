package com.samagra.workflowengine.workflow;

import com.samagra.ancillaryscreens.data.prefs.CommonsPrefsHelperImpl;
import com.samagra.commons.MetaDataExtensions;
import com.samagra.commons.models.metadata.CompetencyModel;
import com.samagra.workflowengine.workflow.model.FlowConfig;

import java.util.ArrayList;
import java.util.List;


public class WorkflowUtils {


    public static ArrayList<FlowConfig> getWorkflowConfigForCompetencies(List<CompetencyModel> competencyList, CommonsPrefsHelperImpl prefs) {
        ArrayList<FlowConfig> flowConfigList = new ArrayList<>();
        if (competencyList != null) {
            for (CompetencyModel competency : competencyList) {
                FlowConfig flowConfig = new FlowConfig();
                flowConfig.setGradeNumber(competency.getGrade());
                String subject = MetaDataExtensions.INSTANCE.getSubjectFromId(competency.getSubjectId(), prefs.getSubjectsListJson());
                flowConfig.setSubject(subject);
                flowConfig.setCompetencyId(competency.getCId() + "");
                List<Integer> states = new ArrayList<>();
                states.add(competency.getFlowState());
                flowConfig.setStates(states);
                flowConfigList.add(flowConfig);
            }
        }
        return flowConfigList;
    }
}
