package com.oneassist.ccf.contract;

import java.util.List;

public class ConfigRequest {
    private String category;
    private String service;
    private List<Stage> stages;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public List<Stage> getStages() {
        return stages;
    }

    public void setStages(List<Stage> stages) {
        this.stages = stages;
    }

    public static class Stage {
        private String stageName;
        private List<Field> fields;
        private List<Document> documents;
        private List<Action> actions;

        public String getStageName() {
            return stageName;
        }

        public void setStageName(String stageName) {
            this.stageName = stageName;
        }

        public List<Field> getFields() {
            return fields;
        }

        public void setFields(List<Field> fields) {
            this.fields = fields;
        }

        public List<Document> getDocuments() {
            return documents;
        }

        public void setDocuments(List<Document> documents) {
            this.documents = documents;
        }

        public List<Action> getActions() {
            return actions;
        }

        public void setActions(List<Action> actions) {
            this.actions = actions;
        }
    }

    public static class Field {
        private String name;
        private String type;
        private boolean mandatory;
        private String validation;
        private List<String> options;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public boolean isMandatory() {
            return mandatory;
        }

        public void setMandatory(boolean mandatory) {
            this.mandatory = mandatory;
        }

        public String getValidation() {
            return validation;
        }

        public void setValidation(String validation) {
            this.validation = validation;
        }

        public List<String> getOptions() {
            return options;
        }

        public void setOptions(List<String> options) {
            this.options = options;
        }
    }

    public static class Document {
        private String name;
        private String mandatory;
        private List<String> allowedFormat;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getMandatory() {
            return mandatory;
        }

        public void setMandatory(String mandatory) {
            this.mandatory = mandatory;
        }

        public List<String> getAllowedFormat() {
            return allowedFormat;
        }

        public void setAllowedFormat(List<String> allowedFormat) {
            this.allowedFormat = allowedFormat;
        }
    }

    public static class Action {
        private String option;
        private String stage;
        private List<Condition> conditions;

        public String getOption() {
            return option;
        }

        public void setOption(String option) {
            this.option = option;
        }

        public String getStage() {
            return stage;
        }

        public void setStage(String stage) {
            this.stage = stage;
        }

        public List<Condition> getConditions() {
            return conditions;
        }

        public void setConditions(List<Condition> conditions) {
            this.conditions = conditions;
        }
    }

    public static class Condition{
        private String field;
        private String operator;
        private String value;

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public String getOperator() {
            return operator;
        }

        public void setOperator(String operator) {
            this.operator = operator;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
