package com.smf.tutorial.process;

import java.util.Date;
import java.util.List;

import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.DalBaseProcess;

import com.smf.tutorial.data.StudentEnrollment;

public class UpdateEnrollmentBackground extends DalBaseProcess {

  @Override
  protected void doExecute(ProcessBundle bundle) throws Exception {
    try {
      Date now = new Date();

      // Create a criteria to fetch all student enrollments
      OBCriteria<StudentEnrollment> studentsCourseCrit = OBDal.getInstance().createCriteria(StudentEnrollment.class);
      studentsCourseCrit.add(Restrictions.lt(StudentEnrollment.PROPERTY_ENDCOURSEDATE, now));
      List<StudentEnrollment> studentsCourseList = studentsCourseCrit.list();

      for (StudentEnrollment studentCourse : studentsCourseList) {
        studentCourse.setActive(false);
        OBDal.getInstance().save(studentCourse);
      }
      OBDal.getInstance().flush();
    } catch (Exception e) {
      throw new OBException(String.format(OBMessageUtils.messageBD("smft_error_exp_date"), e.getMessage()));
    }
  }
}
