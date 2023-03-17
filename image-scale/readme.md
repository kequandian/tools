# 图片压缩到指定大小

1. 使用前准备

    1. 安装python3运行环境

    2. 安装

        ```
        pip install pillow
        pip install loguru
        ```

        

2. 使用方法

    1. 查看帮助 -h

        ```
        python3 scaleImage.py -h
        ```

    2. 压缩单图片 -f

        ```
        python3 scaleImage.py -f image.jpg
        ```

    3. 压缩目录图片 -d

        ```
        python3 scaleImage.py -f imageDir
        ```

    4. 重新压缩 -r

        ```
        python3 scaleImage.py -f imageDir -r
        ```

    5. 输出到指定文件夹

        ```
        python3 scaleImage.py -f imageDir -r -o outputDir
        ```

    6. 指定压缩大小 -s 默认1024kb 可填单位 m或M或g或G,kb不填单位直接填数字

        ```
        python3 scaleImage.py -f imageDir -r -o outputDir -s 2048
        python3 scaleImage.py -f imageDir -r -o outputDir -s 2m
        ```

        

