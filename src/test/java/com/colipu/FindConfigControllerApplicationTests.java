package com.colipu;

import com.colipu.dto.Result;
import com.colipu.service.IFindConfigService;
import com.colipu.service.impl.FindConfigServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class FindConfigControllerApplicationTests {

    @Resource
    private IFindConfigService findConfigService;


    @Test
    void testMethod() throws Exception {
        Result result = findConfigService.findConfig("mse_prepaid_public_cn-2r42fir9y0f", "5dd37175-b149-4a53-8799-ebcb704d0ce0",
                1, 500, "#判断如果有环境变量则使用环境变量");
        System.out.println(result);
    }

}
