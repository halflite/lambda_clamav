# lambda_clamav

AWS Lambda Function for ClamAV

## install aws cli for Windows

Windows cmd

```cmd
msiexec.exe /i https://awscli.amazonaws.com/AWSCLIV2.msi
aws configure
```

## build image

Windows cmd

```cmd
docker build -t lambda_clamav .
docker build -t lambda_clamav:0.0.1 .
```
## push image

Windows cmd

```cmd
aws configure
SET ecr_url=***
SET region=***

aws ecr get-login-password --region %region% | docker login --username AWS --password-stdin %ecr_url%

docker tag lambda_clamav:latest %ecr_url%/lambda_clamav:latest
docker push %ecr_url%/lambda_clamav:latest

docker tag lambda_clamav:0.0.1 %ecr_url%/lambda_clamav:0.0.1
docker push %ecr_url%/lambda_clamav:0.0.1
```
