# PROJECT-4. Перенос стиля на мобильном устройстве

Этот проект реализует мобильное приложение для переноса стиля между изображениями с помощью нейронной сети, обученной с использованием модели [**pytorch-AdaIN**](https://github.com/naoto0804/pytorch-AdaIN?tab=readme-ov-file). Я отказался от использования предлагаемой в задании модели [Magenta](https://github.com/magenta/magenta), так как приложенные инструкции предполагают использование окружения, которое в 2026 году крайне сложно правильно настроить. К тому же многие, кто выполнял этот проект, также описывают процесс использования модели Magenta как сложный, занимающий несколько месяцев из-за устаревших инструкций.


## Возможности

- Загрузка изображения-контента и изображения-стиля
- Перенос стиля прямо (локально) с использованием скрипта `test.py` (файл `Запуск.txt` содержит пример команды)
- Создано [мобильное приложение для ОС Android](https://drive.google.com/file/d/1MxltAPLJDAQOLIvrtEeC7WnGv0xR16Ie/view?usp=drivesdk)
- Используется Pytorch + Torchscript для переноса в мобильное приложение.
- Использована предобученная модель AdaIN.

## Используемые технологии

- Python 3.11
- Pytorch 2.10 + CUDA 12.8
- Модель AdaIN (https://github.com/naoto0804/pytorch-AdaIN?tab=readme-ov-file) с весами из репозитория
- Android Studio + язык Kotlin


## Структура проекта

- `input` - каталог изображений
    - `input/content` - примеры загружаемых изображений
    - `input/style`- примеры загружаемых стилей
- `Mobile Project` - проект для создания мобильного приложения
- `models` - каталог содержит загруженные из основного репозитория веса `vgg_normalised.pth` и `decoder.pth`, а также конвертированные в torchscript веса `adain_style.pt`.
- `output` - каталог с примером наложения стиля input/style/mondrian.jpg на изображение `input/content/brad_pitt.jpg` с alpha от 0.1 до 1.0.
- `video` - содержит видеофайл `20260222_194556.mp4` с доказательством работы [приложения](https://drive.google.com/file/d/1MxltAPLJDAQOLIvrtEeC7WnGv0xR16Ie/view?usp=drivesdk).
- `export_torchscript.py` - скрипт экспорта модели в torchscript.
- `function.py` - файл из репозитория [**pytorch-AdaIN**](https://github.com/naoto0804/pytorch-AdaIN?tab=readme-ov-file) с функциями алгоритма AdaIN.
- `net.py` - файл из репозитория [**pytorch-AdaIN**](https://github.com/naoto0804/pytorch-AdaIN?tab=readme-ov-file) с архитектурой модели.
- `test.py` – файл из репозитория [**pytorch-AdaIN**](https://github.com/naoto0804/pytorch-AdaIN?tab=readme-ov-file), скрипт для тестирования на изображениях.
- `test_exported.py` - скрипт для тестирования модели, экспортированной при помощи `export_torchscript.py`.
- `train.py` - файл из репозитория [**pytorch-AdaIN**](https://github.com/naoto0804/pytorch-AdaIN?tab=readme-ov-file), обучение модели AdaIN.
- `Запуск.txt` - пример использования скрипта `test.py`.

[Ссылка](https://drive.google.com/file/d/1MxltAPLJDAQOLIvrtEeC7WnGv0xR16Ie/view?usp=drivesdk) для загрузки мобильного приложения.