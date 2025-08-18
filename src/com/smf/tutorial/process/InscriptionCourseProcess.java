package com.smf.tutorial.process;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.client.application.process.BaseProcessActionHandler;
import org.apache.log4j.Logger;
import org.openbravo.dal.service.OBDal;

import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.erpCommon.utility.OBMessageUtils;


import com.smf.tutorial.data.CourseSubject;
import com.smf.tutorial.data.StudentEnrollment;
import com.smf.tutorial.events.AutoUpdateCourseDuration;

public class InscriptionCourseProcess extends BaseProcessActionHandler {
  private static final Logger LOG = Logger.getLogger(InscriptionCourseProcess.class);
  private final String DATEPATTERN = "yyyy-MM-dd";

  @Override
  protected JSONObject doExecute(Map<String, Object> parameters, String content) throws JSONException {
    JSONObject result = new JSONObject();
    JSONObject msg = new JSONObject();

    try {
      JSONObject request = new JSONObject(content);
      JSONObject params = request.getJSONObject("_params");

      String dateFromStr = params.getString("startdate");
      SimpleDateFormat format = new SimpleDateFormat(DATEPATTERN);
      Date enrollmentStartDate = format.parse(dateFromStr);

      final String COURSEEDITIONID = params.getString("Smft_Course_Subject_ID");
      final String STUDENTID = params.getString("C_BPartner_ID");

      CourseSubject courseEdition = OBDal.getInstance().get(CourseSubject.class, COURSEEDITIONID);
      BusinessPartner student = OBDal.getInstance().get(BusinessPartner.class, STUDENTID);
      StudentEnrollment studentEnrollment = OBProvider.getInstance().get(StudentEnrollment.class);

      // Check if the student is already enrolled; if so, return an error message
      AutoUpdateCourseDuration.checkDuplicateEnrollment(STUDENTID, COURSEEDITIONID);

      // Set properties for the new student enrollment
      studentEnrollment.setBusinessPartner(student);
      studentEnrollment.setCourseEdition(courseEdition);
      studentEnrollment.setStartCourseDate(enrollmentStartDate);
      studentEnrollment.setEndCourseDate(studentEnrollment.getEndCourseDate());

      OBDal.getInstance().save(studentEnrollment);
      OBDal.getInstance().flush();

      JSONArray actions = new JSONArray();
      result.put("responseActions", actions);
      msg.put("severity", "success");
      msg.put("text", String.format(OBMessageUtils.messageBD("smft_student_enroll_success")));
      result.put("message", msg);
    } catch (Exception e) {
      OBDal.getInstance().rollbackAndClose();
      try {
        OBDal.getInstance().rollbackAndClose();
        msg.put("severity", "error");
        msg.put("text", e.getMessage());
        result.put("message", msg);
        return result;
      } catch (JSONException ex) {
        LOG.error(ex.getMessage());
      }
    }
    return result;
  }
}
