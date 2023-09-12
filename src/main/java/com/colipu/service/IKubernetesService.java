package com.colipu.service;

import com.colipu.model.dto.DeploymentsAndImages.DeploymentsAndImagesDTO;

import java.util.List;

public interface IKubernetesService {

    List<DeploymentsAndImagesDTO> findDeploymentsAndImages(String nameSpace);

}
