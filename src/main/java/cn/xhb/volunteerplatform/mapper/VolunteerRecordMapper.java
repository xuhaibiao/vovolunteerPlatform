package cn.xhb.volunteerplatform.mapper;

import cn.xhb.volunteerplatform.dto.WorkerGetVolunteerEvaluateInfoResponse;
import cn.xhb.volunteerplatform.entity.Volunteer;
import cn.xhb.volunteerplatform.entity.VolunteerRecord;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VolunteerRecordMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(VolunteerRecord record);

    int insertSelective(VolunteerRecord record);

    VolunteerRecord selectByPrimaryKey(Integer id);

    int countByVolunteerIdAndActivityId(Integer activityId, Integer volunteerId);

    int countByActivityIdAndStatus(Integer activityId, Integer status);


    List<VolunteerRecord> selectByVolunteerId(Integer volunteerId);

    List<VolunteerRecord> selectByActivityIdAndStatus(Integer activityId, Integer status);

    List<VolunteerRecord> selectByActivityIdAndTwoStatus(Integer activityId, Integer status1, Integer status2);

    /**
     * 不包括取消报名和被拒绝的记录
     * @param activityId
     * @return
     */
    int countByActivity(Integer activityId);

    int updateByPrimaryKeySelective(VolunteerRecord record);

    int updateByPrimaryKey(VolunteerRecord record);

    List<Volunteer> selectVolunteerInfoByActivityId(Integer activityId);

    List<WorkerGetVolunteerEvaluateInfoResponse> selectVolunteerEvaluateInfoByActivityId(Integer activityId);
}