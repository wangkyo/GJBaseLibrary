package tv.guojiang.baselib.network.response;

import com.google.gson.reflect.TypeToken;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import java.lang.reflect.Type;
import org.json.JSONObject;
import tv.guojiang.baselib.network.NetworkExceptionTransformer;
import tv.guojiang.baselib.network.config.ServerCode;
import tv.guojiang.baselib.network.exception.ApiException;
import tv.guojiang.baselib.util.JsonUtils;

/**
 * 对分页的json数据进行解析
 * <pre>
 * 分页接口数据：
 *     {"errno":0, "msg":"", "data":[]}
 * </pre>
 *
 * @author leo
 */
public class PagerNetworkTransformer<T> implements ObservableTransformer<String, PagerResponse<T>> {

    /**
     * "data"中每个Item对象的类型
     */
    private Class<T> mItemClazz;

    public PagerNetworkTransformer(Class<T> itemClazz) {
        mItemClazz = itemClazz;
    }

    @Override
    public ObservableSource<PagerResponse<T>> apply(Observable<String> upstream) {
        return upstream
            .map(json -> {

                // {"errno":0, "msg":"", "data":[]}
                JSONObject jsonObject = new JSONObject(json);
                int code = jsonObject.optInt("errno",ServerCode.SERVER_ERROR);

                // 封装业务错误
                if (code != ServerCode.SUCCESS) {
                    String msg = jsonObject.optString("msg", "No error message from serve!");
                    throw new ApiException(code, msg);
                }

                // 解析数据
                Type type = TypeToken.getParameterized(PagerResponse.class, mItemClazz).getType();
                return JsonUtils.getInstance().<PagerResponse<T>>fromJson(json, type);
            })
            .compose(new NetworkExceptionTransformer<>());
    }
}
