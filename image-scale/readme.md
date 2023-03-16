# 图片压缩到指定大小

1. 使用前准备

    1. 安装python3运行环境

    2. 安装

        ```
        pip install pillow
        pip install loguru
        ```

        

2. 使用方法

    1. ```
        python3 scaleImage.py [-f|-d] [inputFile|inputDir] [outputFile|outputDir] [imageSize]
        ```

        1. 示例
            1. 压缩单个图片
                1. python3 scaleImage.py -f test.jpg output.jpg 2048
            2. 压缩目录图片
                1. python3 scaleImage.py -d test output 2048

    2. 参数说明

        1. -f 表示压缩单个图片 inputFile 表示需要压缩的文件，outputFile 表示压缩后的文件
        2. -d 表示压缩整个目录里面的图片，inputDir表示需要压缩的文件夹，outputdir表示输出到目录文件夹
        3. imageSIze 为可选参数 默认1024，表示图片压缩后不超过这个值，单位kb