import torch
from PIL import Image
from torchvision import transforms

# Загружаем модель
model = torch.jit.load('models/adain_style.pt')
model.eval()

# Пример предобработки изображения
def load_image(path, size):
    img = Image.open(path).convert('RGB').resize((size, size))
    transform = transforms.ToTensor()  # [0,1]
    return transform(img).unsqueeze(0)  # (1,3,H,W)

content = load_image('input/content/avril.jpg', 384)
style = load_image('input/style/mondrian_cropped.jpg', 256)

with torch.no_grad():
    output = model(content, style)

# Сохраняем результат
output_img = transforms.ToPILImage()(output.squeeze(0).clamp(0, 1))
output_img.save('output/stylized.jpg')
print("Готово!")