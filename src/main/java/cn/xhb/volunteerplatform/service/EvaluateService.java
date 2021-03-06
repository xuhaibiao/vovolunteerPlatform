package cn.xhb.volunteerplatform.service;

import cn.xhb.volunteerplatform.constant.RecordConstant;
import cn.xhb.volunteerplatform.entity.Activity;
import cn.xhb.volunteerplatform.entity.Volunteer;
import cn.xhb.volunteerplatform.entity.VolunteerRecord;
import cn.xhb.volunteerplatform.mapper.ActivityMapper;
import cn.xhb.volunteerplatform.mapper.VolunteerMapper;
import cn.xhb.volunteerplatform.mapper.VolunteerRecordMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

@Service
public class EvaluateService {
    @Resource
    VolunteerRecordMapper volunteerRecordMapper;
    @Resource
    ActivityMapper activityMapper;
    @Resource
    VolunteerMapper volunteerMapper;


    public int volunteerEvaluate(Float score, String content, Integer volunteerRecordId, Integer recordPreStatus, String picInfo) {
        VolunteerRecord volunteerRecord = new VolunteerRecord();
        volunteerRecord.setId(volunteerRecordId);
        volunteerRecord.setVolunteerEvaluateScore(score);
        volunteerRecord.setVolunteerEvaluateContent(content);
        volunteerRecord.setPicInfo(picInfo);
        volunteerRecord.setStatus(RecordConstant.VOLUNTEER_HAS_EVALUATE_WORKER_NOT);
//        if (recordPreStatus == RecordConstant.ACTIVITY_IS_OVER) {
//            volunteerRecord.setStatus(RecordConstant.VOLUNTEER_HAS_EVALUATE_WORKER_NOT);
//        } else if (recordPreStatus == RecordConstant.WORKER_HAS_EVALUATE_VOLUNTEER_NOT) {
//            volunteerRecord.setStatus(RecordConstant.ALL_HAS_EVALUATE);
//        }
        volunteerRecord.setUpdateTime(new Date());
        return volunteerRecordMapper.updateByPrimaryKeySelective(volunteerRecord);

        // 更新志愿者工时
//        VolunteerRecord v = volunteerRecordMapper.selectByPrimaryKey(volunteerRecordId);
//        Activity activity = activityMapper.selectByPrimaryKey(v.getActivityId());
//        Volunteer volunteer = volunteerMapper.selectByPrimaryKey(v.getVolunteerId());
//        volunteer.setVolunteerHours(volunteer.getVolunteerHours() + activity.getWorkingHours());
//        int i = volunteerMapper.updateByPrimaryKeySelective(volunteer);
//        if (i > 0) {
//            return volunteerRecordMapper.updateByPrimaryKeySelective(volunteerRecord);
//        } else {
//            return i;
//        }


    }

    public int workerEvaluate(Float score, Integer volunteerRecordId, Integer recordPreStatus) {
        VolunteerRecord volunteerRecord = new VolunteerRecord();
        volunteerRecord.setId(volunteerRecordId);
        volunteerRecord.setWorkerEvaluate(score);
        volunteerRecord.setStatus(RecordConstant.ALL_HAS_EVALUATE);
//        if (recordPreStatus == RecordConstant.ACTIVITY_IS_OVER) {
//            volunteerRecord.setStatus(RecordConstant.WORKER_HAS_EVALUATE_VOLUNTEER_NOT);
//        } else if (recordPreStatus == RecordConstant.VOLUNTEER_HAS_EVALUATE_WORKER_NOT) {
//            volunteerRecord.setStatus(RecordConstant.ALL_HAS_EVALUATE);
//        }
        volunteerRecord.setUpdateTime(new Date());
        // 更新志愿者分数、工时、志愿次数
        VolunteerRecord v = volunteerRecordMapper.selectByPrimaryKey(volunteerRecordId);
        Activity activity = activityMapper.selectByPrimaryKey(v.getActivityId());
        Integer volunteerId = v.getVolunteerId();
        Volunteer volunteer = volunteerMapper.selectByPrimaryKey(volunteerId);
        volunteer.setVolunteerHours(volunteer.getVolunteerHours() + activity.getWorkingHours());
        volunteer.setVolunteerScore(volunteer.getVolunteerScore() + score);
        volunteer.setVolunteerNumber(volunteer.getVolunteerNumber() + 1);
        int i = volunteerMapper.updateByPrimaryKeySelective(volunteer);
        if (i > 0) {
            return volunteerRecordMapper.updateByPrimaryKeySelective(volunteerRecord);
        } else {
            return i;
        }


    }

    public int workerRefuseEvaluate(Integer volunteerRecordId) {
        VolunteerRecord volunteerRecord = new VolunteerRecord();
        volunteerRecord.setId(volunteerRecordId);
        // 设置状态为审核未通过
        volunteerRecord.setStatus(RecordConstant.VOLUNTEER_APPROVE_FAILED);
        volunteerRecord.setUpdateTime(new Date());
        return volunteerRecordMapper.updateByPrimaryKeySelective(volunteerRecord);
    }

}
