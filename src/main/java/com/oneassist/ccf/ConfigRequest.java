package com.oneassist.ccf;


import java.util.List;

public class ConfigRequest {
    private String categoryName;
    private List<Stage> stages;

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
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
    }
}
