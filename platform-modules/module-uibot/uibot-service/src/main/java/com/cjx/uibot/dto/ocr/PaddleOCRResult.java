package com.cjx.uibot.dto.ocr;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PaddleOCRResult {
    @JsonProperty("model_settings")
    private ModelSettings modelSettings;
    @JsonProperty("doc_preprocessor_res")
    private DocPreprocessorRes docPreprocessorRes;
    @JsonProperty("text_det_params")
    private TextDetParams textDetParams;
    @JsonProperty("text_type")
    private String textType;
    @JsonProperty("textline_orientation_angles")
    private List<Integer> textlineOrientationAngles;
    @JsonProperty("text_rec_score_thresh")
    private Double textRecScoreThresh;
    @JsonProperty("return_word_box")
    private Boolean returnWordBox;
    @JsonProperty("rec_polys")
    private List<List<List<Integer>>> recPolys;

    @JsonProperty("rec_texts")
    private List<String> recTexts;

    @JsonProperty("rec_scores")
    private List<Double> recScores;

    @JsonProperty("rec_boxes")
    private List<List<Integer>> recBoxes;

    @JsonProperty("dt_polys")
    private List<List<List<Integer>>> dtPolys;

    public static class ModelSettings {
        @JsonProperty("use_doc_preprocessor")
        private Boolean useDocPreprocessor;
        @JsonProperty("use_textline_orientation")
        private Boolean useTextlineOrientation;

        public Boolean getUseDocPreprocessor() { return useDocPreprocessor; }
        public void setUseDocPreprocessor(Boolean useDocPreprocessor) { this.useDocPreprocessor = useDocPreprocessor; }

        public Boolean getUseTextlineOrientation() { return useTextlineOrientation; }
        public void setUseTextlineOrientation(Boolean useTextlineOrientation) { this.useTextlineOrientation = useTextlineOrientation; }
    }

    public static class DocPreprocessorRes {
        @JsonProperty("model_settings")
        private ModelSettings modelSettings;
        private Integer angle;

        public ModelSettings getModelSettings() { return modelSettings; }
        public void setModelSettings(ModelSettings modelSettings) { this.modelSettings = modelSettings; }

        public Integer getAngle() { return angle; }
        public void setAngle(Integer angle) { this.angle = angle; }
    }

    public static class TextDetParams {
        @JsonProperty("limit_side_len")
        private Integer limitSideLen;
        @JsonProperty("limit_type")
        private String limitType;
        private Double thresh;
        @JsonProperty("max_side_limit")
        private Integer maxSideLimit;
        @JsonProperty("box_thresh")
        private Double boxThresh;
        @JsonProperty("unclip_ratio")
        private Double unclipRatio;

        public Integer getLimitSideLen() { return limitSideLen; }
        public void setLimitSideLen(Integer limitSideLen) { this.limitSideLen = limitSideLen; }

        public String getLimitType() { return limitType; }
        public void setLimitType(String limitType) { this.limitType = limitType; }

        public Double getThresh() { return thresh; }
        public void setThresh(Double thresh) { this.thresh = thresh; }

        public Integer getMaxSideLimit() { return maxSideLimit; }
        public void setMaxSideLimit(Integer maxSideLimit) { this.maxSideLimit = maxSideLimit; }

        public Double getBoxThresh() { return boxThresh; }
        public void setBoxThresh(Double boxThresh) { this.boxThresh = boxThresh; }

        public Double getUnclipRatio() { return unclipRatio; }
        public void setUnclipRatio(Double unclipRatio) { this.unclipRatio = unclipRatio; }
    }
}
