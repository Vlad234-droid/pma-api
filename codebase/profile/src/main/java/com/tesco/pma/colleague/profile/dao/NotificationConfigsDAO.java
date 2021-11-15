package com.tesco.pma.colleague.profile.dao;

import com.tesco.pma.colleague.profile.domain.NotificationAttribute;
import com.tesco.pma.colleague.profile.domain.NotificationAttributeType;
import org.apache.ibatis.annotations.Param;

public interface NotificationConfigsDAO {

    NotificationAttribute findByType(@Param("type") NotificationAttributeType notificationAttributeType);

}
