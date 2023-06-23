package proj.research.colortools;

import org.junit.Test;

import static org.junit.Assert.*;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import proj.research.colortools.bean.ResInfo;
import proj.research.colortools.bean.TaskData;
import proj.research.colortools.bean.TrainInfo;
import proj.research.colortools.util.Local;


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void test1() throws Exception {
        System.out.println("");
        System.out.println("");
        List<Double[]> data = Local.getThreeXData(4, (x) -> (3.0 * x[0] + 4.0 * x[1] + 6.0 * x[2]));
        List<List<Double>> input = new ArrayList<>();
        List<Double> output = new ArrayList<>();

        for (Double[] d : data) {
            List<Double> perInput = new ArrayList<>();
            perInput.add(d[0]);
            perInput.add(d[1]);
            perInput.add(d[2]);
            input.add(perInput);
            output.add(d[3]);
        }

        TaskData taskData = new TaskData();
        taskData.feats = input;
        taskData.targets = output;
        taskData.trainRatio = 0.66;
        taskData.allowError = 0.66;

        String resJsonStr = Local.post("/dl/train", JSON.toJSONString(taskData));
        String trainId = (String) JSON.parseObject(resJsonStr, ResInfo.class).getMsg();
        while (true) {
            //每隔2s查询一次训练情况
            Thread.sleep(2000);
            String checkJson = Local.post("/dl/check", "{\"trainingId\":\"" + trainId + "\"}");
            JSONObject jo = (JSONObject) JSON.parseObject(checkJson, ResInfo.class).getMsg();
            TrainInfo trainInfo = jo.toJavaObject(TrainInfo.class);
            System.out.println(trainInfo);
            if(trainInfo.isDone()){
                System.out.println("over");
                break;
            }
        }
    }
}