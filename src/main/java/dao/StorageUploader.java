package dao;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;

public class StorageUploader {
    private BlobContainerClient containerClient;

    public StorageUploader( ) {
        this.containerClient = new BlobContainerClientBuilder()
                .connectionString("DefaultEndpointsProtocol=https;AccountName=thomascsc311storage;AccountKey=26+AvvdtOjgls5h9rOl3rej8TRGP5IhoVSxmGt1RaG5FxYQAEG/qbKFPPpfbOfpuv9SQiYIpPzef+AStsyYvxA==;EndpointSuffix=core.windows.net")
                .containerName("media-files")
                .buildClient();
    }

    public void uploadFile(String filePath, String blobName) {
        BlobClient blobClient = containerClient.getBlobClient(blobName);
        blobClient.uploadFromFile(filePath,true);
    }
    public BlobContainerClient getContainerClient(){
        return containerClient;
    }
}
