import torch
import torch.nn as nn
from net import vgg as vgg_arch, decoder as decoder_arch
from function import adaptive_instance_normalization

class StyleTransferModel(nn.Module):
    def __init__(self, encoder, decoder):
        super().__init__()
        self.encoder = encoder
        self.decoder = decoder

    def forward(self, content, style, alpha=1.0):
        content_feat = self.encoder(content)
        style_feat = self.encoder(style)
        t = adaptive_instance_normalization(content_feat, style_feat)
        t = alpha * t + (1 - alpha) * content_feat
        return self.decoder(t)

def main():
    # Загружаем архитектуры
    vgg = vgg_arch
    decoder = decoder_arch

    # Обрезаем VGG до relu4_1 (как в test.py)
    encoder = nn.Sequential(*list(vgg.children())[:31])

    # Загружаем веса
    vgg.load_state_dict(torch.load('models/vgg_normalised.pth', map_location='cpu'))
    decoder.load_state_dict(torch.load('models/decoder.pth', map_location='cpu'))

    # Создаём модель
    model = StyleTransferModel(encoder, decoder)
    model.eval()

    # Фиксированные размеры для мобильного приложения
    content_h, content_w = 384, 384
    style_h, style_w = 256, 256

    # Примеры входных тензоров (batch_size=1)
    content = torch.randn(1, 3, content_h, content_w)
    style = torch.randn(1, 3, style_h, style_w)

    # Трассировка модели
    traced_model = torch.jit.trace(model, (content, style))

    # Сохраняем
    traced_model.save('models/adain_style.pt')
    print("Модель успешно экспортирована в adain_style.pt")

if __name__ == '__main__':
    main()