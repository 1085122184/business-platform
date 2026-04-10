package com.cjx.uibot.controller;

import cn.hutool.core.lang.UUID;
import com.alibaba.fastjson2.JSON;
import com.cjx.uibot.entity.UiBotProcess;
import com.cjx.uibot.entity.UiBotProcessStatus;
import com.cjx.uibot.service.UiBotService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/uiBot")
@RequiredArgsConstructor
public class UiBotController {
    private final UiBotService uiBotService;
    private final Queue<UiBotProcess> uiBotQueue;
    private final Map<String, UiBotProcessStatus> statusCache;

    @Resource(name = "ioExecutor")
    private ThreadPoolTaskExecutor ioExecutor;

    @GetMapping("test/{name}/{id}")
    public List<Map<String,String>> getName(@PathVariable("name") String name, @PathVariable("id") String id){
        List<Map<String,String>> list = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            Map<String,String> map = new HashMap<>();
            map.put("name",name+i);
            map.put("id",id+i);
            list.add(map);
        }

        System.out.println(name+id);
        return list;
    }

    @GetMapping("{TriggerName}")
    public void startProcess(@PathVariable("TriggerName") String TriggerName){
        Map<String, Object> map = new HashMap<>();
        map.put("TriggerName",TriggerName);
        uiBotService.startProcess(JSON.toJSONString(map));
    }

    @GetMapping("/{TriggerName}/{businessId}")
    public void startProcessOnFirst(@PathVariable("TriggerName") String TriggerName,@PathVariable("businessId") String businessId){
        UiBotProcess uiBotProcess = new UiBotProcess();
        uiBotProcess.setId(UUID.randomUUID().toString());
        uiBotProcess.setBusinessId(businessId);
        uiBotProcess.setTriggerName(TriggerName);
//        UiBotProcessStatus processStatus = statusService.list().get(0);
        UiBotProcessStatus processStatus = statusCache.get("status");
        extracted(uiBotProcess, processStatus);
    }



    @GetMapping("success/{businessId}")
    public void success(@PathVariable("businessId") String businessId){
        UiBotProcessStatus processStatus = new UiBotProcessStatus();
        processStatus.setProcessStatus("0");
        statusCache.put("status",processStatus);
        if (uiBotQueue.size()>0){
            ioExecutor.execute(() -> extracted(uiBotQueue.poll(), processStatus));
        }

    }

    @GetMapping("error/{businessId}")
    public void error(@PathVariable("businessId") String businessId){
        UiBotProcessStatus processStatus = new UiBotProcessStatus();
        processStatus.setProcessStatus("0");
        statusCache.put("status",processStatus);
        if (uiBotQueue.size()>0){
            ioExecutor.execute(() -> extracted(uiBotQueue.poll(), processStatus));
        }
    }

    @GetMapping()
    public void getTask(){
       Iterator<UiBotProcess> uiBotProcessIterator = uiBotQueue.iterator();
       while (uiBotProcessIterator.hasNext()){
           System.out.println(uiBotProcessIterator.next().getId());
       }
        System.out.println("当前流程状态"+statusCache.get("status").getProcessStatus());
    }


    private void extracted(UiBotProcess uiBotProcess, UiBotProcessStatus processStatus) {
        if (processStatus.getProcessStatus().equals("1")){
            uiBotProcess.setStatus("0");
            uiBotQueue.offer(uiBotProcess);
        }else {
            uiBotProcess.setStatus("1");
            Map<String, Object> map = new HashMap<>();
            map.put("TriggerName", uiBotProcess.getTriggerName());
            processStatus.setProcessStatus("1");
            processStatus.setUibotId(uiBotProcess.getId());
//            statusService.updateById(processStatus);
            statusCache.put("status", processStatus);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            uiBotService.startProcess(JSON.toJSONString(map));
        }
//        processService.save(uiBotProcess);
    }
}
