package com.smartmedical.dto;

import lombok.Data;

@Data
public class MedicalRecordUpdateReq {
    private String chiefComplaint;
    private String presentIllness;
    private String diagnosis;
    private String treatmentAdvice;
}