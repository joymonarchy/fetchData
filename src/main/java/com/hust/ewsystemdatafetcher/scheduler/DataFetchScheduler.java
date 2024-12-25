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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


import java.util.*;

@Component
public class DataFetchScheduler {

    // API连接参数
    private static final String API_HOST = "192.168.10.77";
    private static final int API_PORT = 9090;
    private static final String API_USERNAME = "admin";
    private static final String API_PASSWORD = "123456";

    @Autowired
    private DataService dataService;

    @Autowired
    private RealPointService realPointService;

    /**
     * 每30秒执行一次
     */
    @Scheduled(fixedRate = 30000)
    public void fetchData() {
        IYFApi connect = null;
        try {
            // 连接API
            connect = YFFactory.CreateApi(API_HOST, API_PORT, API_USERNAME, API_PASSWORD);

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