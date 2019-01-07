package org.geelato.web.platform.rest;


import net.oschina.j2cache.CacheChannel;
import net.oschina.j2cache.CacheObject;
import net.oschina.j2cache.J2Cache;
import org.geelato.core.api.ApiPagedResult;
import org.geelato.core.api.ApiResult;
import org.geelato.core.meta.MetaManager;
import org.geelato.core.mvc.MediaTypes;
import org.geelato.core.orm.Dao;
import org.geelato.web.platform.entity.CacheItemMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.HashMap;

/**
 * :
 *
 * @author itechgee@126.com
 * @date 2019/1/1.
 */
@Controller
@RequestMapping(value = "/api/cache/")
public class CacheController {

    private CacheChannel cache = J2Cache.getChannel();
    @Autowired
    @Qualifier("primaryDao")
    protected Dao dao;

    private MetaManager metaManager = MetaManager.singleInstance();


    private static Logger logger = LoggerFactory.getLogger(CacheController.class);

    /**
     * e.g.:http://localhost:8080/api/cache/list/
     *
     * @param request
     * @return
     */
    @RequestMapping(value = {"regions", "regions/*"}, method = RequestMethod.POST, produces = MediaTypes.JSON_UTF_8)
    @ResponseBody
    public ApiResult list(HttpServletRequest request) {
        ApiResult apiResult = new ApiResult();
        apiResult.setData(cache.regions());
        HashMap<String, Collection> result = null;
        for (CacheChannel.Region region : cache.regions()) {
            for (String key : cache.keys(region.getName())) {
                CacheObject cacheObject = cache.get(region.getName(), key);
                cacheObject.getKey();
                cacheObject.getLevel();
                cacheObject.getRegion();
                cacheObject.getValue();
            }
        }

        ApiPagedResult page = new ApiPagedResult();
        page.setDataSize(10);
        page.setPage(1000);
        page.setSize(10);
        page.setTotal(1000);
        page.setData(page);
        page.setMeta(metaManager.get(CacheItemMeta.class).getSimpleFieldMetas(new String[]{"region", "key", "level", "value"}));
        return page;
    }
}
