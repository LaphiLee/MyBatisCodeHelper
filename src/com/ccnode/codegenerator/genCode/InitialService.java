package com.ccnode.codegenerator.genCode;

import com.ccnode.codegenerator.pojo.GenCodeResponse;
import com.ccnode.codegenerator.pojo.OnePojoInfo;
import com.ccnode.codegenerator.pojo.PojoFieldInfo;
import com.ccnode.codegenerator.pojoHelper.GenCodeResponseHelper;
import com.ccnode.codegenerator.pojoHelper.OnePojoInfoHelper;
import com.ccnode.codegenerator.util.GenCodeConfig;
import com.ccnode.codegenerator.util.GenCodeUtil;
import com.ccnode.codegenerator.util.IOUtils;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

/**
 * What always stop you is what you always believe.
 * <p>
 * Created by zhengjun.du on 2016/08/27 11:09
 */
public class InitialService {

    private final static Logger LOGGER = LoggerFactory.getLogger(InitialService.class);

    public static GenCodeResponse initPojos(GenCodeResponse response) {
        List<OnePojoInfo> contextList = Lists.newArrayList();
        response.setPojoInfos(contextList);
        for (String pojoName : response.getRequest().getPojoNames()) {
            OnePojoInfo onePojoInfo = new OnePojoInfo();
            try{
                onePojoInfo.setPojoName(pojoName);
                onePojoInfo.setPojoClassSimpleName(pojoName);
                File pojoFile = IOUtils.matchOnlyOneFile(response.getRequest().getProjectPath(), pojoName + ".java");
                if(pojoFile == null){
                    return response.failure("", pojoName + " file not exist");
                }
                String projectPath = GenCodeResponseHelper.getProjectPathWithSplitter(response);
                GenCodeConfig config = response.getCodeConfig();
                String fullPojoPath = pojoFile.getAbsolutePath();
                String pojoDirPath = pojoFile.getParentFile().getAbsolutePath();
                onePojoInfo.setFullDaoPath(genPath(response, pojoDirPath, config.getDaoDir(), pojoName,"Dao.java"));
                onePojoInfo.setFullServicePath(genPath(response, pojoDirPath, config.getServiceDir(), pojoName,"Service.java"));
                onePojoInfo.setFullSqlPath(genPath(response, pojoDirPath, config.getSqlDir(), pojoName,".sql"));
                onePojoInfo.setFullMapperPath(genPath(response, pojoDirPath, config.getMapperDir(), pojoName,"Mapper.xml"));
                onePojoInfo.setFullPojoPath(fullPojoPath);
                OnePojoInfoHelper.parseIdeaFieldInfo(onePojoInfo, response);
                onePojoInfo.setDaoPackage(GenCodeUtil
                        .deducePackage(StringUtils.defaultIfEmpty(config.getDaoDir(),pojoDirPath) ,onePojoInfo.getPojoPackage()));
                onePojoInfo.setServicePackage(GenCodeUtil.deducePackage(StringUtils.defaultIfEmpty(config.getServiceDir(),pojoDirPath) ,onePojoInfo.getPojoPackage()));
                List<PojoFieldInfo> pojoFieldInfos = onePojoInfo.getPojoFieldInfos();
                String concat = StringUtils.EMPTY;
                for (PojoFieldInfo pojoFieldInfo : pojoFieldInfos) {
                    concat += "|"+pojoFieldInfo.getFieldName();
                }
                if(!concat.contains("id")){
                    LOGGER.error(pojoName + " should has 'id' field");
                    return response.failure(pojoName + " should has 'id' field");
                }
                OnePojoInfoHelper.parseFiles(onePojoInfo,response);
                contextList.add(onePojoInfo);
            }catch(Exception e){
                LOGGER.error("parse Class:"+pojoName + "failure",e);
                return response.failure("","parse Class:"+pojoName + "failure");
            }
        }
        return response;
    }

    private static String genPath(GenCodeResponse response, String pojoDirPath, String configDir, String pojoName, String fileSuffix) {
        String projectPath = GenCodeResponseHelper.getProjectPathWithSplitter(response);
        File file = IOUtils.matchOnlyOneFile(projectPath, pojoName + fileSuffix);
        if(file != null){
            return file.getAbsolutePath();
        }
        if(StringUtils.isBlank(configDir)){
            return pojoDirPath + response.getPathSplitter() +pojoName + fileSuffix;
        }
        return projectPath + configDir + response.getPathSplitter() +pojoName + fileSuffix;
    }

    private static void checkPathChange(OnePojoInfo onePojoInfo){


    }

}
