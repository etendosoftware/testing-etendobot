package com.smf.tutorial.process;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.client.application.process.BaseProcessActionHandler;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;

import com.smf.tutorial.data.CourseSubject;
import com.smf.tutorial.data.StudentEnrollment;

public class SetScoreStudentProcess extends BaseProcessActionHandler {
  private static final Logger LOG = Logger.getLogger(SetScoreStudentProcess.class);

  @Override
  protected JSONObject doExecute(Map<String, Object> parameters, String content) throws JSONException {
    JSONObject result = new JSONObject();
    JSONObject msg = new JSONObject();

    try {
      JSONObject request = new JSONObject(content);
      JSONObject params = request.getJSONObject("_params");

      final String courseEditionParams = params.getString("Smft_Course_Subject_ID");
      final String studentParams = params.getString("Smft_Student_Enrollment_ID");
      final long scoreParams = params.getLong("Score");

      CourseSubject courseEdition = OBDal.getInstance().get(CourseSubject.class, courseEditionParams);
      StudentEnrollment studentEnrollment = OBDal.getInstance().get(StudentEnrollment.class, studentParams);

      final String courseEditionId = courseEdition.getId();
      final String studentCourseEditionId = studentEnrollment.getCourseEdition().getId();
      final String studentName = studentEnrollment.getBusinessPartner().getName();

      if (scoreParams < 1 || scoreParams > 10) {
        throw new OBException(String.format(
            OBMessageUtils.messageBD("smft_score_error_number")));
      }

      if (!StringUtils.equals(courseEditionId, studentCourseEditionId)) {
        throw new OBException(String.format(
            OBMessageUtils.messageBD("smft_error_score_student"), studentName));
      }
      
      studentEnrollment.setScore(scoreParams);
      OBDal.getInstance().save(studentEnrollment);
      OBDal.getInstance().flush();

      JSONArray actions = new JSONArray();
      result.put("responseActions", actions);
      msg.put("severity", "success");
      msg.put("text", String.format(OBMessageUtils.messageBD("smft_success_score_student"), studentName));
      result.put("message", msg);

    } catch (Exception e) {
      OBDal.getInstance().rollbackAndClose();
      msg.put("severity", "error");
      msg.put("text", e.getMessage());
      result.put("message", msg);
      return result;
    }

    return result;
  }
}
