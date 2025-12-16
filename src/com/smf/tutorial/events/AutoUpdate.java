package com.smf.tutorial.events;

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
  }
}
