package com.colipu.model.dto.DeploymentsAndImages;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeploymentsAndImagesDTO {

    private String deploymentName;

    private String imageName;

    private String imageRepoName;
}
