package com.smf.tutorial.events;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.plm.Product;
import org.openbravo.erpCommon.utility.OBMessageUtils;

import javax.enterprise.event.Observes;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.smf.tutorial.data.CourseSubject;
import com.smf.tutorial.data.StudentEnrollment;

public class AutoUpdateCourseDuration extends EntityPersistenceEventObserver {

  private static final Entity[] entities = {
      ModelProvider.getInstance().getEntity(StudentEnrollment.ENTITY_NAME)
  };

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  public void onSave(@Observes EntityNewEvent event) {
    if (!isValidEvent(event)) {
      return;
    }

    final CourseSubject courseEdition = (CourseSubject) event.getCurrentState(
        event.getTargetInstance().getEntity().getProperty(StudentEnrollment.PROPERTY_COURSEEDITION));
    final StudentEnrollment student = (StudentEnrollment) event.getTargetInstance();
    final Product currentCourse = courseEdition.getProduct();

    // Calculate end date course and set it in the entity
    Date endDate = calculateEndDate(student.getStartCourseDate(),
        currentCourse.getSmftCourseDurationMonths().intValue());
    final Property endDateCourseProperty = student.getEntity().getProperty(StudentEnrollment.PROPERTY_ENDCOURSEDATE);
    event.setCurrentState(endDateCourseProperty, endDate);

    // Check if the student is already enrolled in the same course edition
    final String STUDENTID = student.getBusinessPartner().getId();
    final String COURSEDURATIONID = courseEdition.getId();
    checkDuplicateEnrollment(STUDENTID, COURSEDURATIONID);
  }

  /**
   * Checks if the student with the given ID is already enrolled in the same course edition.
   *
   * @param studentId
   *     The ID of the student to check for enrollment.
   * @param courseEditionId
   *     The ID of the course edition to check for enrollment.
   */
  public static void checkDuplicateEnrollment(String studentId, String courseEditionId) {
    final OBCriteria<StudentEnrollment> allStudents = OBDal.getInstance().createCriteria(StudentEnrollment.class);
    CourseSubject courseEdition = OBDal.getInstance().get(CourseSubject.class, courseEditionId);

    allStudents.add(Restrictions.eq(StudentEnrollment.PROPERTY_COURSEEDITION, courseEdition));
    List<StudentEnrollment> allStudentsEnrollList = allStudents.list();

    for (StudentEnrollment enrolledStudent : allStudentsEnrollList) {
      BusinessPartner enrolledBusinessPartner = enrolledStudent.getBusinessPartner();

      // Use the ID comparison directly
      if (StringUtils.equals(enrolledBusinessPartner.getId(), studentId)) {
        throw new OBException(String.format(
            OBMessageUtils.messageBD("smft_student_enrolled_error"), enrolledBusinessPartner.getName()));
      }
    }
  }

  /**
   * Calculates the end date based on the start date and duration in months.
   *
   * @param startDate
   *     The start date.
   * @param durationMonths
   *     The duration in months.
   * @return The calculated end date.
   */
  private static Date calculateEndDate(Date startDate, int durationMonths) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(startDate);
    calendar.add(Calendar.MONTH, durationMonths);
    return calendar.getTime();
  }
}
