package cn.xhb.volunteerplatform.controller;

import cn.xhb.volunteerplatform.constant.ActivityConstant;
import cn.xhb.volunteerplatform.constant.RecordConstant;
import cn.xhb.volunteerplatform.dto.*;
import cn.xhb.volunteerplatform.entity.CommunityOrganization;
import cn.xhb.volunteerplatform.entity.Volunteer;
import cn.xhb.volunteerplatform.service.*;
import cn.xhb.volunteerplatform.util.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/volunteer")
public class VolunteerController {

    @Resource
    ActivityService activityService;
    @Resource
    UserService userService;
    @Resource
    EvaluateService evaluateService;
    @Resource
    CommunityService communityService;
    @Resource
    MessageService messageService;


    @GetMapping("/activity")
    public Result<List<ActivityResponse>> getActivities(){
        List<ActivityResponse> rs = activityService.getNotDeletedActivities();
        return Result.success(rs);
    }

    @GetMapping("/activityInfo")
    public Result<ActivityResponse> getActivityInfo(@RequestParam("activityId") Integer activityId){
        ActivityResponse rs = activityService.getActivityInfoById(activityId);
        return Result.success(rs);
    }


    @GetMapping("/activity/search")
    public Result<List<ActivityResponse>> getActivitiesBySearch(@RequestParam("province") String province,@RequestParam("city") String city,@RequestParam("area") String area,@RequestParam("activityName") String activityName){
        ActivitySearchQuery activitySearchQuery = new ActivitySearchQuery();

        if("省".equals(province)){
            activitySearchQuery.setProvince(null);
            activitySearchQuery.setCity(null);
            activitySearchQuery.setArea(null);
        } else if ("市".equals(city)) {
            activitySearchQuery.setProvince(province);
            activitySearchQuery.setCity(null);
            activitySearchQuery.setArea(null);
        } else if ("区".equals(area)) {
            activitySearchQuery.setProvince(province);
            activitySearchQuery.setCity(city);
            activitySearchQuery.setArea(null);
        } else {
            activitySearchQuery.setProvince(province);
            activitySearchQuery.setCity(city);
            activitySearchQuery.setArea(area);
        }
        if (StringUtils.isNotBlank(activityName)) {
            activitySearchQuery.setActivityName(activityName);
        } else {
            activitySearchQuery.setActivityName(null);
        }
        List<ActivityResponse> rs = activityService.getNotDeletedActivitiesBySearch(activitySearchQuery);
        return Result.success(rs);
    }

    @PostMapping("/activity/signUp")
    public Result<Object> activitySignUp(@RequestBody ActivitySignUpRequest activitySignUpRequest) {
        int type = activityService.activitySignUp(activitySignUpRequest.getActivityId(), activitySignUpRequest.getUserId());
        if (type == ActivityConstant.HAS_SIGNED_UP) {
            return Result.error("您已报名过该活动！","warning");
        } else if (type == ActivityConstant.SIGNED_UP_ERROR) {
            return Result.error("报名失败！","error");
        } else if (type == ActivityConstant.HAS_DELETE) {
            return Result.error("活动已删除！", "error");
        } else {
            return Result.success(null);
        }

    }



    @GetMapping("/record")
    public Result<List<VolunteerRecordResponse>> getVolunteerRecords(@RequestParam Integer userId) {
        List<VolunteerRecordResponse> rs = userService.getVolunteerRecordsByVolunteerId(userId);
        return Result.success(rs);
    }

    @PostMapping("/record/evaluate")
    public Result<String> evaluate(VolunteerEvaluateRequest evaluateRequest, MultipartFile file, HttpServletRequest req){
        Result<String> upload = FileUtils.uploadPic(file, req);
        if (upload.getCode() == 1) {
            int i = evaluateService.volunteerEvaluate(evaluateRequest.getScore(), evaluateRequest.getContent(), evaluateRequest.getRecordId(), evaluateRequest.getRecordStatus(), upload.getData());
            if (i > 0) {
                return Result.success(null);
            } else {
                return Result.error("评价失败！");
            }
        } else {
            return upload;
        }

    }

    @PostMapping("/record/cancelSignUp")
    public Result<Object> cancelSignUp(@RequestBody VolunteerCancelSignUpRequest volunteerCancelSignUpRequest){
        // 报名已经通过的前提下，需要发送取消报名消息，todo：后期可以加上惩罚措施
        if (volunteerCancelSignUpRequest.getVolunteerRecord().getStatus() == RecordConstant.REGISTRATION_PASSED) {
            Volunteer v = userService.getVolunteerById(volunteerCancelSignUpRequest.getVolunteerRecord().getVolunteerId());
            String volunteerName = v.getName();
            String volunteerIdCard = v.getIdCard();
            Integer volunteerId = v.getId();

            int k = messageService.addCancelSignUpMsg(volunteerCancelSignUpRequest.getActivityId(), volunteerCancelSignUpRequest.getActivityName()
                    , volunteerCancelSignUpRequest.getCommunityId(), volunteerName, volunteerIdCard, volunteerId);
            if (k == 0) {
                return Result.error();
            }
        }
        int i = userService.cancelSignUp(volunteerCancelSignUpRequest.getVolunteerRecord());
        if (i > 0) {
            return Result.success(null);
        } else {
            return Result.error();
        }
    }

    @GetMapping("/community")
    public Result<VolunteerGetCommunityResponse> getCommunities(@RequestParam("volunteerId") Integer volunteerId) {
        Volunteer volunteer = userService.getVolunteerById(volunteerId);
        VolunteerGetCommunityResponse communityResponse = new VolunteerGetCommunityResponse();
        List<CommunityOrganization> rs = communityService.getNotDeletedCommunities();
        communityResponse.setCommunityOrganizations(rs);
        CommunityOrganization community = communityService.getCommunity(volunteer.getCommunityId());
        communityResponse.setUserCommuntity(community);
        communityResponse.setVolunteer(volunteer);
        return Result.success(communityResponse);
    }

    @GetMapping("/community/search")
    public Result<VolunteerGetCommunityResponse> getCommunityBySearch(@RequestParam("province") String province,@RequestParam("city") String city,@RequestParam("area") String area,@RequestParam("communityName") String communityName) {
        CommunitySearchQuery communitySearchQuery = new CommunitySearchQuery();
        if("省".equals(province)){
            communitySearchQuery.setProvince(null);
            communitySearchQuery.setCity(null);
            communitySearchQuery.setArea(null);
        } else if ("市".equals(city)) {
            communitySearchQuery.setProvince(province);
            communitySearchQuery.setCity(null);
            communitySearchQuery.setArea(null);
        } else if ("区".equals(area)) {
            communitySearchQuery.setProvince(province);
            communitySearchQuery.setCity(city);
            communitySearchQuery.setArea(null);
        } else {
            communitySearchQuery.setProvince(province);
            communitySearchQuery.setCity(city);
            communitySearchQuery.setArea(area);
        }
        if (StringUtils.isNotBlank(communityName)) {
            communitySearchQuery.setCommunityName(communityName);
        } else {
            communitySearchQuery.setCommunityName(null);
        }
        List<CommunityOrganization> rs = communityService.getNotDeletedCommunitiesBySearch(communitySearchQuery);
        VolunteerGetCommunityResponse volunteerGetCommunityResponse = new VolunteerGetCommunityResponse();
        volunteerGetCommunityResponse.setCommunityOrganizations(rs);
        return Result.success(volunteerGetCommunityResponse);
    }

    @PostMapping("/community/quit")
    public Result<Object> quitCommunity(@RequestBody Volunteer volunteer){
        int i = communityService.quitCommunity(volunteer.getId(), volunteer.getName(), volunteer.getIdCard(), volunteer.getCommunityId());
        if (i > 0) {
            return Result.success(null);
        } else {
            return Result.error();
        }
    }

    @PostMapping("/community/join")
    public Result<Object> joinCommunity(@RequestBody JoinRequest joinRequest){
        int i = communityService.joinCommunity(joinRequest);
        if (i > 0) {
            return Result.success(null);
        } else if (i == -1) {
            return Result.error("已经申请过该组织，请耐心等待工作人员审核！");
        } else {
            return Result.error("申请出错！");
        }
    }

    @GetMapping("/message")
    public Result<MessageResponse> getMessages(@RequestParam("volunteerId") Integer volunteerId) {
        Volunteer v = userService.getVolunteerById(volunteerId);
        MessageResponse rs = messageService.getVolunteerMessages(v);
        return Result.success(rs);
    }



}
