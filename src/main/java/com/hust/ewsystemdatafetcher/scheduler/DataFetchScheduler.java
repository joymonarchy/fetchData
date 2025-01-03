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
//    @Scheduled(fixedRate = 10000)//十分钟一次试试呢
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
            Date startTime = cal.getTime();

            cal.add(Calendar.SECOND, 600);
            Date endTime = cal.getTime();

            long interval = 10 * 1000; // 每次查询间隔10秒

// 将 vCpids 分批，每批处理 100 个
            int batchSize = 100;
            List<YFHisval> allValues = new ArrayList<>();

            for (int i = 0; i < vCpids.size(); i += batchSize) {
                // 获取当前批次的 vCpids
                int end = Math.min(i + batchSize, vCpids.size());
                List<String> batchCpids = vCpids.subList(i, end);

                // 获取当前批次的数据
                List<YFHisval> values = connect.GetHistoryValue(batchCpids, startTime, endTime, interval);

                // 将当前批次的数据添加到所有结果中
                allValues.addAll(values);
            }

// 现在 allValues 包含了所有批次的数据

            dataService.processAndSaveHisData(allValues);


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