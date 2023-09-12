package com.colipu.service;

import com.colipu.model.dto.Result;

public interface IPullAndPushDockerService {

    void pullAndTagAndPushImage(String imageName);

}
