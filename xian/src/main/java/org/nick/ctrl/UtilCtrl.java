package org.nick.ctrl;

import org.apache.commons.lang3.StringUtils;
import org.fla.nnd.s1.Cx;
import org.kx.util.DateUtil;
import org.kx.util.FileUtil;
import org.kx.util.base.LocalCacheManager;
import org.kx.util.base.ResultRich;
import org.kx.util.base.StringUtil;
import org.kx.util.ddl.IDBSQLFormat;
import org.kx.util.dto.RemoteMechine;
import org.kx.util.mail.MailSendUtil;
import org.kx.util.remote.JarCompare3;
import org.kx.util.remote.RemoteLog;
import org.kx.util.rsa.RsaCommon;
import org.nick.dto.SimpleObject;
import org.nick.util.ServletUitl;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * Description ：
 * Created by  -------
 * Since 2019/12/3
 */

@RestController
@RequestMapping("/util")
public class UtilCtrl {

    @RequestMapping("/idbsql")
    public String idbsql(@RequestParam String str) {
        return IDBSQLFormat.dealStr(str);
    }


    @RequestMapping("/mergeLine")
    public String mergeLine(@RequestParam String str, @RequestParam String LengthLimit) {

        if (StringUtils.isNotBlank(LengthLimit)) {
            return StringUtil.mergeLine(str, Long.parseLong(LengthLimit));
        }
        return StringUtil.mergeLine(str);
    }

    @RequestMapping("/toLine")
    public String toLine(@RequestParam String str) {
        return StringUtil.toLine(str);
    }


    @RequestMapping("/getMiddleString")
    public String getMiddleString(@RequestParam String str, @RequestParam String prx, @RequestParam String sux) {

        StringBuilder sbt = new StringBuilder();
        String[] ss = str.split("\n");
        for (String sss : ss) {
            String middleString = StringUtil.getMiddleString(sss, prx, sux).trim();
            if (StringUtils.isNotBlank(middleString)) {
                if (sbt.length() > 0) {
                    sbt.append("\n").append(StringUtil.getMiddleString(sss, prx, sux).trim());
                } else {
                    sbt.append(StringUtil.getMiddleString(sss, prx, sux).trim());
                }
            }
        }
        return sbt.toString();
    }


    @RequestMapping("/getMiddleString2")
    public String getMiddleString2(@RequestParam String str, @RequestParam int prx) {

        StringBuilder sbt = new StringBuilder();
        String[] ss = str.split("\n");
        for (String sss : ss) {
            String[] ssr = sss.split(" ");
            int currentIndex = 0;
            for (String strd : ssr) {
                if (StringUtils.isNotBlank(strd)) {
                    currentIndex++;
                    if (currentIndex == prx) {
                        if (sbt.length() > 0) {
                            sbt.append("\n").append(strd);
                        } else {
                            sbt.append(strd);
                        }
                    }
                }
            }
        }
        return sbt.toString();
    }


    @RequestMapping("/getRemoteLog")
    public ResultRich getRemoteLog(@RequestBody RemoteMechine remoteMechine) {
        try {
            checkPass(remoteMechine);
            String msg = new RemoteLog().getLastPaylog(remoteMechine);
            return new ResultRich(Boolean.TRUE, msg);
        } catch (Exception e) {
            return new ResultRich(Boolean.FALSE, e.getMessage());
        }
    }


    @RequestMapping("/getRemoteJarDiff")
    public ResultRich getRemoteJarDiff(@RequestBody RemoteMechine remoteMechine) {
        try {
            checkPass(remoteMechine);
            String msg = new JarCompare3().actionPerformed(remoteMechine);
            return new ResultRich(Boolean.TRUE, msg);
        } catch (Exception e) {
            return new ResultRich(Boolean.FALSE, e.getMessage());
        }
    }


    private void checkPass(RemoteMechine remoteMechine) throws IOException {
        String userName = remoteMechine.getUserName();
        String userPass = remoteMechine.getUserPass();
        String localPass = (String) LocalCacheManager.getInstance().getCache(userName);
        if (StringUtils.isBlank(userPass) && StringUtils.isNotBlank(localPass)) {
            remoteMechine.setUserPass(localPass);
        }
        if (StringUtils.isBlank(userPass) && StringUtils.isBlank(localPass)) {
            throw new IllegalArgumentException("not found user pass");
        }

        if (StringUtils.isNotBlank(userPass)) {
            userPass = StringUtil.unicode2String(userPass);
            if (!userPass.equals(localPass)) {
                LocalCacheManager.getInstance().addCache(userName, userPass);
            }
            remoteMechine.setUserPass(userPass);
        }
    }

    @RequestMapping("/savePass")
    public ModelAndView savePass(@RequestParam String userName, @RequestParam String userPass, HttpServletRequest request, HttpServletResponse response) throws IOException {
        LocalCacheManager.getInstance().addCache(userName, userPass);
        ModelAndView mv = new ModelAndView("redirect:/pages/getRemoteJarDiff.html?userName=" + userName);//默认为forward模式
        return mv;
    }


    @RequestMapping("/removePass")
    public ModelAndView removePass(@RequestParam String userName) throws IOException {
        LocalCacheManager.getInstance().removeCache(userName);
        ModelAndView mv = new ModelAndView("redirect:/pages/getRemoteJarDiff.html?userName=" + userName);//默认为forward模式
        return mv;
        //return "remove "+userName +"!";
    }


    @RequestMapping("/isSafe")
    public ResultRich safe(HttpServletRequest request) throws IOException {
        String ip = ServletUitl.getIpAddress(request);
        String userName = (String) LocalCacheManager.getInstance().getCache(ip);
        if (StringUtils.isNotBlank(userName)) {
            return new ResultRich(Boolean.TRUE, ip);
        } else {
            return new ResultRich(Boolean.FALSE, ip);
        }
    }


    @RequestMapping("/readLocal")
    public String readLocal(String path) throws IOException {
        return FileUtil.readFile(path);
    }


    @RequestMapping("/sendRemoteBm")
    public String sendRemoteBm(String path) {
        try {
            String exstr = Cx.encrypt(path);
            MailSendUtil.sendCommonMail("From 09", exstr);
        } catch (Exception e) {
            return e.getMessage();
        }
        return "success";
    }

    @RequestMapping("/sendRemote")
    public String sendRemote(String path) {
        try {
            MailSendUtil.sendCommonMail("From 08", path);
        } catch (Exception e) {
            return e.getMessage();
        }
        return "success";
    }

    @RequestMapping("/save")
    public String save(@RequestBody SimpleObject simpleObject) {
        try {
            String filePath = System.getProperty("user.home") + "/temp/"+DateUtil.getDateTimeStr(new Date(),"yyyyMMdd")+".txt";
            FileUtil.appedStringToFile("\n"+simpleObject.getContent(),filePath);
        } catch (Exception e) {
            return e.getMessage();
        }
        return "success";
    }


    @RequestMapping("/putKey")
    public String putKey() throws Exception {
        String filePath = "/home/xianguang.skx/default.txt";
        RsaCommon.getKeys(filePath);
        return "success";
    }


    @RequestMapping("/readLocal2")
    public String readLocal2(String path) throws IOException {
        String readLocal = FileUtil.readFile(path);
        return Cx.encrypt(readLocal);
    }


    @RequestMapping("/listLocal")
    public String listLocal(String path) throws Exception {
        List<File> f = FileUtil.showListFile(new File(path));
        StringBuilder sbt = new StringBuilder();
        for (File file : f) {
            if (sbt.length() == 0) {
                sbt.append(file.getPath());
            } else {
                sbt.append("\n").append(file.getPath());
            }
        }
        return sbt.toString();
    }


    @RequestMapping("/2Safe")
    public ResultRich addSafe(HttpServletRequest request, @RequestBody RemoteMechine remoteMechine) throws IOException {
        try {
            String ip = ServletUitl.getIpAddress(request);
            if (!"skx".equals(remoteMechine.getUserName())) {
                return new ResultRich(Boolean.FALSE, "非法账号");
            }
            LocalCacheManager.getInstance().addCache(ip, remoteMechine.getUserName());
            return new ResultRich(Boolean.TRUE, ip);
        } catch (Exception e) {
            return new ResultRich(Boolean.FALSE, e.getMessage());
        }
    }


    @PostMapping("/upload")
    @ResponseBody
    public String upload(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return "上传失败，请选择文件";
        }
        String fileName = file.getOriginalFilename();
        // String dir = System.getProperty("user.home");
        // String filePath = dir + "/temp/";

        String filePath = "/home/xianguang.skx/";
        FileUtil.createDir(filePath);
        File dest = new File(filePath + fileName);
        try {
            file.transferTo(dest);
            return "上传成功: " + dest.getAbsolutePath();
        } catch (IOException e) {
            return "上传失败！" + e.getMessage();
        }
    }


}
