# Amazon EC2 plugin

This features is used to spin up jenkins slave hosted by the AWS EC2 service.

# How to use it ?

## Envionment variables
This groovy script uses environment variables to store sensitive data, thus be sure to configure the following variables :

- AWS_ACCESS_KEY_ID : The AWS access key
- AWS_SECRET_ACCESS_KEY : The AWS secret key
- EC2_CONFIG : The path to the json file containing the plugin configuration
- EC2_PRIVATE_KEY : The private key used to configure the jenkins slaves
- IAM_ARN_SLAVE : The AWS IAM role of the jenkins slaves

## EC2 configuration file

To create the ec2.json file structure we used the code hosted on https://plugins.jenkins.io/ec2.

Each field is named as per the sample code shared on the EC2 plugin sites.

```
[
    {
        "cloudType": "amazonEC2Cloud",
        "useInstanceProfileForCredentials": false,
        "cloudName": "myCloud",
        "region": "us-east-2",
        "instanceCapStr": "",
        "slavesTemplate": [
            {
                "ami": "ami-965e6bf3",
                "associatePublicIp": false,
                "connectBySSHProcess": true,
                "connectUsingPublicIp": false,
                "customDeviceMapping": "/dev/sda1=:500",
                "deleteRootOnTermination": true,
                "description": "Ubuntu 16.04",
                "ebsOptimized": false,
                "idleTerminationMinutes": "5",
                "initScript": [
                    "export TERM=xterm",
                    "sudo apt-get install -y curl unzip wget",
                    "sudo echo \"* * * * 7 root docker system prune -f\" >> /etc/crontab"
                ],
                "instanceCapStr": "",
                "jvmopts": "-Xmx3g",
                "labelString": "",
                "launchTimeoutStr": "",
                "numExecutors": "5",
                "remoteAdmin": "ubuntu",
                "remoteFS": "/home/ubuntu",
                "securityGroups": "Jenkins Linux Slave",
                "stopOnTerminate": false,
                "subnetId": "subnet-dd0b9990",
                "tags": {
                    "group": "DevOps"
                },
                "tmpDir": "",
                "type": "t2.medium",
                "useDedicatedTenancy": false,
                "useEphemeralDevices": false,
                "usePrivateDnsName": false,
                "userData": "",
                "zone": ""
            }
        ]
    }
]
```
## Limitation

- Currently the feature only support only amazon cloud, this limitation is due to the way we use to pass the AWS keys and the private SSH key.
- Currently the feature only support one slave template per cloud, this limitation is due to the way we use to pass the IAM profile ARN.