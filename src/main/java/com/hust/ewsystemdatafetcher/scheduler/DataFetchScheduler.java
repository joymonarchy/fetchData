package com.hust.ewsystemdatafetcher.scheduler;

/**
 * @Author piiaJet
 * @Create 2024/12/2417:15
 */


import com.hust.ewsystemdatafetcher.service.DataService;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.hust.ewsystemdatafetcher.service.RealPointService;
import com.yingfeng.api.IYFApi;
import com.yingfeng.api.YFFactory;
import com.yingfeng.api.YFHisval;
import com.yingfeng.api.YFNowval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


import java.util.*;

@Component
public class DataFetchScheduler {

    // API连接参数
    @Value("${api.host}")
    private String apiHost;

    @Value("${api.port}")
    private int apiPort;

    @Value("${api.username}")
    private String apiUsername;

    @Value("${api.password}")
    private String apiPassword;


    @Autowired
    private DataService dataService;

    @Autowired
    private RealPointService realPointService;

    /**
     * 每30秒执行一次
     */
    @Scheduled(fixedRate = 10000)
    public void fetchNowData(){
        IYFApi connect = null;
        try {
            // 连接API
            connect = YFFactory.CreateApi(apiHost, apiPort, apiUsername, apiPassword);

            // 从数据库获取vcpids
            List<String> vCpids = realPointService.getAllVcpids();

            if (vCpids.isEmpty()) {
                System.out.println("未从数据库获取到vcpids.");
                return;
            }

            // 获取当前值
            Calendar cal = new GregorianCalendar();
            cal.add(Calendar.MINUTE, -30);


            List<YFNowval> values = connect.GetNowValue(vCpids);
            dataService.processAndSaveNowData(values);


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭API连接
            if (connect != null) {
                try {
                    connect.Close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public void fetchHisData() {
        IYFApi connect = null;
        try {
            // 连接API
            connect = YFFactory.CreateApi(apiHost, apiPort, apiUsername, apiPassword);

            // 从数据库获取vcpids
            List<String> vCpids = realPointService.getAllVcpids();

            if (vCpids.isEmpty()) {
                System.out.println("未从数据库获取到vcpids.");
                return;
            }

            // 获取当前值
            Calendar cal = new GregorianCalendar();
            cal.add(Calendar.MINUTE, -30);

            // 获取调整后的时间
            Date snapTime = cal.getTime();

            List<YFHisval> values = connect.GetSnapshot(vCpids,snapTime);
            dataService.processAndSaveHisData(values);


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭API连接
            if (connect != null) {
                try {
                    connect.Close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}