package cn.xhb.volunteerplatform.service;

import cn.xhb.volunteerplatform.constant.ActivityConstant;
import cn.xhb.volunteerplatform.constant.RecordConstant;
import cn.xhb.volunteerplatform.dto.ActivityResponse;
import cn.xhb.volunteerplatform.dto.ReviewTableResponse;
import cn.xhb.volunteerplatform.dto.VolunteerRecordResponse;
import cn.xhb.volunteerplatform.entity.*;
import cn.xhb.volunteerplatform.mapper.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class UserService {
    @Resource
    AdministratorMapper administratorMapper;
    @Resource
    VolunteerMapper volunteerMapper;
    @Resource
    WorkerMapper workerMapper;
    @Resource
    VolunteerRecordMapper volunteerRecordMapper;
    @Resource
    ActivityMapper activityMapper;
    @Resource
    CommunityOrganizationMapper communityOrganizationMapper;

    public Volunteer getVolunteerById(Integer id){
        return volunteerMapper.selectByPrimaryKey(id);
    }

    public Volunteer getVolunteerByIdCard(String idCard) {
        return volunteerMapper.selectByIdCard(idCard);
    }

    public int updateVolunteer(Volunteer volunteer) {
        return volunteerMapper.updateByPrimaryKeySelective(volunteer);
    }

    public Worker getWorkerByIdCard(String idCard) {
        return workerMapper.selectByIdCard(idCard);
    }

    public Administrator getAdministratorByIdCard(String idCard) {
        return administratorMapper.selectByIdCard(idCard);
    }

    public List<VolunteerRecordResponse> getVolunteerRecordsByVolunteerId(Integer volunteerId){
        List<VolunteerRecord> volunteerRecords = volunteerRecordMapper.selectByVolunteerId(volunteerId);
        List<VolunteerRecordResponse> rs = new ArrayList<>();
        for (VolunteerRecord volunteerRecord : volunteerRecords) {
            VolunteerRecordResponse volunteerRecordResponse = new VolunteerRecordResponse();
            volunteerRecordResponse.setVolunteerRecord(volunteerRecord);
            Integer activityId = volunteerRecord.getActivityId();
            Activity activity = activityMapper.selectByPrimaryKey(activityId);
            Date now = new Date();
            // 记录状态
            if (activity.getHasDeleted() == 1) {
                volunteerRecord.setStatus(RecordConstant.ACTIVITY_HAS_DELETED);
            }else if (activity.getActivityBeginTime().before(now) && activity.getActivityEndTime().after(now) && volunteerRecord.getStatus() == ActivityConstant.SIGNED_UP_SUCCESS) {
                volunteerRecord.setStatus(RecordConstant.ACTIVITY_IN_PROGRESS);
            } else if (activity.getActivityEndTime().before(now) && volunteerRecord.getStatus() == ActivityConstant.SIGNED_UP_SUCCESS) {
                volunteerRecord.setStatus(RecordConstant.ACTIVITY_IS_OVER);

            }
            volunteerRecordMapper.updateByPrimaryKeySelective(volunteerRecord);
            ActivityResponse activityResponse = new ActivityResponse();
            activityResponse.setActivity(activity);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String begin = sdf.format(activity.getActivityBeginTime());
            String end = sdf.format(activity.getActivityEndTime());
            activityResponse.setActivityTimeRange(begin + "至" + end);
            Worker worker = workerMapper.selectByPrimaryKey(activity.getWorkerId());
            CommunityOrganization communityOrganization = communityOrganizationMapper.selectByPrimaryKey(worker.getCommunityId());
            activityResponse.setCommunityName(communityOrganization.getName());
            activityResponse.setSponsor(worker.name);
            Date now2 = new Date();
            if(now2.before(activity.getRecruitBeginTime())){
                activityResponse.setActivityStatus(ActivityConstant.RECRUIT_NOT_STARTED);
            } else if (now2.before(activity.getRecruitEndTime())) {
                activityResponse.setActivityStatus(ActivityConstant.RECRUITING);
            } else {
                activityResponse.setActivityStatus(ActivityConstant.RECRUIT_OVER);
            }
            int signedUp = volunteerRecordMapper.countByActivity(activity.getId());
            activityResponse.setHasRecruitedNumber(signedUp);
            volunteerRecordResponse.setActivityResponse(activityResponse);

            rs.add(volunteerRecordResponse);
        }
        return rs;
    }

    public List<ReviewTableResponse> getRegistrationData(Integer activityId) {
        // 0表示报名审核中的记录
        List<VolunteerRecord> volunteerRecords = volunteerRecordMapper.selectByActivityIdAndStatus(activityId, 0);
        List<ReviewTableResponse> rs = new ArrayList<>();
        for (VolunteerRecord volunteerRecord : volunteerRecords) {
            ReviewTableResponse tmp = new ReviewTableResponse();
            Integer volunteerId = volunteerRecord.getVolunteerId();
            Volunteer volunteer = volunteerMapper.selectByPrimaryKey(volunteerId);
            volunteer.setPassword("");
            tmp.setVolunteer(volunteer);
            tmp.setRegistrationTime(volunteerRecord.getCreateTime());
            tmp.setRecordId(volunteerRecord.getId());
            rs.add(tmp);
        }
        return rs;
    }


    public int agreeJoin(Integer recordId) {
        VolunteerRecord volunteerRecord = new VolunteerRecord();
        volunteerRecord.setStatus(RecordConstant.REGISTRATION_PASSED);
        volunteerRecord.setUpdateTime(new Date());
        volunteerRecord.setId(recordId);
        return volunteerRecordMapper.updateByPrimaryKeySelective(volunteerRecord);
    }

    public int refuseJoin(Integer recordId) {
        VolunteerRecord volunteerRecord = new VolunteerRecord();
        volunteerRecord.setStatus(RecordConstant.REGISTRATION_FAILED);
        volunteerRecord.setUpdateTime(new Date());
        volunteerRecord.setId(recordId);
        return volunteerRecordMapper.updateByPrimaryKeySelective(volunteerRecord);
    }

}