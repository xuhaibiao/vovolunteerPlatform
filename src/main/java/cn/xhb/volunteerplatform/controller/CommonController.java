package cn.xhb.volunteerplatform.controller;


import cn.xhb.volunteerplatform.dto.*;
import cn.xhb.volunteerplatform.dto.vo.FiveYearNumberVo;
import cn.xhb.volunteerplatform.entity.Administrator;
import cn.xhb.volunteerplatform.entity.BaseUser;
import cn.xhb.volunteerplatform.entity.Volunteer;
import cn.xhb.volunteerplatform.entity.Worker;
import cn.xhb.volunteerplatform.service.CommunityService;
import cn.xhb.volunteerplatform.service.MessageService;
import cn.xhb.volunteerplatform.service.StatisticsService;
import cn.xhb.volunteerplatform.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @author HaibiaoXu
 * @date Create in 10:41 2021/1/11
 * @modified By
 */
@RestController
public class CommonController {

    @Resource
    UserService userService;
    @Resource
    StatisticsService statisticsService;
    @Resource
    CommunityService communityService;
    @Resource
    MessageService messageService;

    @PostMapping("/login")
    public Result<BaseUser> login(@RequestBody LoginRequest loginRequest) {
        BaseUser user = null;
        int type = loginRequest.getType();
        if (type == 0) {
            user = userService.getVolunteerByIdCard(loginRequest.getIdCard());
        } else if (type == 1) {
            user = userService.getWorkerByIdCard(loginRequest.getIdCard());
        } else if (type == 2) {
            user = userService.getAdministratorByIdCard(loginRequest.getIdCard());
        }

        if (user != null && user.getPassword().equals(loginRequest.getPassword()) ) {
            // 密码不返前端
//            user.setPassword(null);
            return Result.success(user, String.valueOf(type));
        } else{
            return Result.error();
        }
    }

    @PostMapping("/signUp")
    public Result<Object> login(@RequestBody SignUpRequest signUpRequest) {

        try {
            if (signUpRequest.getType() == 0) {
                // 注册志愿者
                Volunteer volunteer = new Volunteer();
                volunteer.setIdCard(signUpRequest.getIdCard());
                volunteer.setName(signUpRequest.getUsername());
                volunteer.setPassword(signUpRequest.getPassword());
                volunteer.setPhone(signUpRequest.getPhone());
                volunteer.setAddress(signUpRequest.getProvince() + signUpRequest.getCity() + signUpRequest.getArea() + signUpRequest.getDetailAddress());
                volunteer.setGender(signUpRequest.getGender());
                volunteer.setCreateTime(new Date());
                volunteer.setVolunteerHours((float) 0);
                volunteer.setVolunteerScore((float) 0);
                volunteer.setVolunteerNumber(0);
                volunteer.setBanStatus(0);
                volunteer.setCommunityId(0);
                int i = userService.addVolunteer(volunteer);
                if (i > 0) {
                    return Result.success(null);
                } else {
                    return Result.error("注册失败！");
                }
            } else if (signUpRequest.getType() == 1) {
                // 注册社区工作者（加入社区）
                Worker worker = new Worker();
                worker.setIdCard(signUpRequest.getIdCard());
                worker.setName(signUpRequest.getUsername());
                worker.setPassword(signUpRequest.getPassword());
                worker.setPhone(signUpRequest.getPhone());
                worker.setAddress(signUpRequest.getProvince() + signUpRequest.getCity() + signUpRequest.getArea() + signUpRequest.getDetailAddress());
                worker.setGender(signUpRequest.getGender());
                worker.setCreateTime(new Date());
                worker.setBanStatus(0);
                // 需要等待社区发起人通过才能设置
                worker.setCommunityId(-1);
                int i = userService.addWorker(worker);
                if (i > 0) {
                    // 解析加入的社区id
                    String joinCommunityInfo = signUpRequest.getJoinCommunityInfo();
                    String s = joinCommunityInfo.split(":")[1];
                    String cid = s.substring(0, s.length() - 1);
                    Integer communityId = Integer.valueOf(cid);
                    // 发送申请加入消息
                    Worker w = userService.getWorkerByIdCard(signUpRequest.getIdCard());
                    int k = messageService.addWorkerJoinCommunityMsg(w, communityId);
                    if (k > 0) {
                        return Result.success(null);
                    } else {
                        return Result.error("注册失败！");
                    }
                } else {
                    return Result.error("注册失败！");
                }

            } else {
                // 注册社区工作者（创建社区）
            }
        } catch (Exception e) {
            return Result.error("注册失败！"+e.getMessage());
        }
        return Result.error("注册失败！");
    }

    @GetMapping("/loadAllCommunity")
    public Result<List<LoadAllCommunityResponse>> getAllCommunity(){
        List<LoadAllCommunityResponse> list = communityService.getAllNotDelCommunity();
        return Result.success(list);
    }


    @GetMapping("/information")
    public Result<BaseUser> getInformation(@RequestParam("userId") Integer userId,@RequestParam("type") Integer type) {
        BaseUser user = null;
        if (type == 0) {
            user = userService.getVolunteerById(userId);
        } else if (type == 1) {
            user = userService.getWorkerById(userId);
        } else if (type == 2) {
            user = userService.getAdministratorById(userId);
        }
        if (user != null) {
//            user.setPassword(null);
            return Result.success(user);
        }else{
            return Result.error();
        }
    }

    @PostMapping("/information/edit")
    public Result<BaseUser> updateVolunteer(@RequestBody InformationEditRequest informationEditRequest){
        if (informationEditRequest.getType() == 0) {
            Volunteer volunteer = new Volunteer();
            BeanUtils.copyProperties(informationEditRequest, volunteer);
            int i = userService.updateVolunteer(volunteer);
            if (i > 0) {
                Volunteer v = userService.getVolunteerById(volunteer.id);
                return Result.success(v);
            } else {
                return Result.error();
            }
        } else if (informationEditRequest.getType() == 1) {
            Worker worker = new Worker();
            BeanUtils.copyProperties(informationEditRequest, worker);
            int i = userService.updateWorker(worker);
            if (i > 0) {
                Worker w = userService.getWorkerById(worker.id);
                return Result.success(w);
            } else {
                return Result.error();
            }
        } else {
            Administrator administrator = new Administrator();
            BeanUtils.copyProperties(informationEditRequest, administrator);
            int i = userService.updateAdministrator(administrator);
            if (i > 0) {
                Administrator a = userService.getAdministratorById(administrator.id);
                return Result.success(a);
            } else {
                return Result.error();
            }
        }

    }

    @GetMapping("/statistics")
    public Result<StatisticsDataResponse> getStatisticsData() {
        StatisticsDataResponse statisticsDataResponse = new StatisticsDataResponse();
        FiveYearNumberVo f = statisticsService.getNumInFiveYear();
        int[] sexRatio = statisticsService.getSexRatio();

        statisticsDataResponse.setFiveYearNumberVo(f);
        statisticsDataResponse.setSexRatio(sexRatio);


        return Result.success(statisticsDataResponse);
    }


    @PostMapping("/upload")
    public Result<String> upload(MultipartFile file, HttpServletRequest req) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        //再用pdf格式开始书写,先找原始的名字
        String originName = file.getOriginalFilename();
//        判断文件类型是不是pdf
        if(!originName.endsWith(".pdf")){
            //如果不是的话，就返回类型
            return Result.error("文件类型不对");

        }

        String format=sdf.format(new Date());
        String realPath = "C:\\Users\\Lucas\\IdeaProjects\\volunteerPlatform\\fileData\\" + format;

        //再是保存文件的文件夹
        File folder = new File(realPath);
        //如果不存在，就自己创建
        if(!folder.exists()){
            folder.mkdirs();
        }
        String newName = UUID.randomUUID().toString() + ".pdf";
        Result<String> rs;
        //然后就可以保存了
        try {
            file.transferTo(new File(folder,newName));
            //这个还有一个url
            String url = req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort() + "/" + realPath + newName;
            //如果指向成功了
            rs = Result.success(url);
        } catch (IOException e) {
            //返回异常
            rs = Result.error(e.getMessage());

        }
        return  rs;

    }


    @GetMapping("/download")
    public void download(@RequestParam("fileName") String fileName, @RequestParam("fileDate") String fileDate,HttpServletRequest req, HttpServletResponse resp) {

        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        OutputStream fos = null;
        try {
            String filePath = "C:\\Users\\Lucas\\IdeaProjects\\volunteerPlatform\\fileData";
            bis = new BufferedInputStream(new FileInputStream(filePath + "/" + fileDate + "/" + fileName));
            fos = resp.getOutputStream();
            bos = new BufferedOutputStream(fos);
            setFileDownloadHeader(req, resp, fileName);
            resp.setContentType("application/octet-stream");
            resp.setHeader("Content-Disposition", "attachment;filename=" + new String(fileName.getBytes(StandardCharsets.UTF_8), "iso-8859-1"));
            int byteRead = 0;
            byte[] buffer = new byte[2048];
            while ((byteRead = bis.read(buffer)) != -1) {
                bos.write(buffer, 0, byteRead);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                assert bos != null;
                bos.flush();
                bis.close();
                fos.close();
                bos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public static void setFileDownloadHeader(HttpServletRequest request,
                                             HttpServletResponse response, String fileName) {
        try {
            String encodedFileName = null;
            String agent = request.getHeader("USER-AGENT");
            if (null != agent && agent.contains("MSIE")) {
                encodedFileName = URLEncoder.encode(fileName, "UTF-8");
            } else if (null != agent && agent.contains("Mozilla")) {
                encodedFileName = new String(fileName.getBytes(StandardCharsets.UTF_8),
                        StandardCharsets.ISO_8859_1);
            } else {
                encodedFileName = URLEncoder.encode(fileName, "UTF-8");
            }

            response.setContentType("application/x-download;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment;filename=\""
                    + encodedFileName + "\"");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




}
