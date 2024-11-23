import os

os.environ['TF_ENABLE_ONEDNN_OPTS'] = '0'
os.environ['KERAS_BACKEND'] = 'tensorflow'

import tensorflow as tf
import keras
import numpy as np


print(keras.__version__)
print(tf.__version__)

disease_types = [
    'Apple___Apple_scab', 'Apple___Black_rot', 'Apple___Cedar_apple_rust', 'Apple___healthy',
    'Cherry___Powdery_mildew', 'Cherry___healthy',
    'Peach___Bacterial_spot', 'Peach___healthy', 'Pepper___bell___Bacterial_spot', 
    'Pepper___bell___healthy', 'Potato___Early_blight', 'Potato___Late_blight', 
    'Potato___healthy', 'Strawberry___Leaf_scorch', 'Strawberry___healthy',
    'Tomato___Bacterial_spot', 'Tomato___Early_blight', 'Tomato___Late_blight',
    'Tomato___Leaf_Mold', 'Tomato___Septoria_leaf_spot', 
    'Tomato___Spider_mites_Two-spotted_spider_mite', 'Tomato___Target_Spot', 
    'Tomato___Tomato_Yellow_Leaf_Curl_Virus', 'Tomato___Tomato_mosaic_virus', 
    'Tomato___healthy'
]



# test_images = "C:/Users/Allan/Pictures/Saved Pictures/adachi.jpg"

# new_model = tf.keras.models.load_model('my_model.keras')

# # Show the model architecture
# new_model.summary()

# # Evaluate the restored model
# loss, acc = new_model.evaluate(test_images, disease_types, verbose=2)
# print('Restored model, accuracy: {:5.2f}%'.format(100 * acc))

# print(new_model.predict(test_images).shape)

# def predict_with_unknown(model, image_path, class_names, threshold=0.5):
#     IMAGE_SIZE = (224, 224)  # Replace with your model's input size

#     # Load a local image file
#     img = tf.keras.preprocessing.image.load_img(image_path, target_size=IMAGE_SIZE)

#     # Preprocess the image
#     img = img.resize(IMAGE_SIZE)  # Resize if using a downloaded image
#     img_array = tf.keras.preprocessing.image.img_to_array(img)
#     img_array = np.expand_dims(img_array, axis=0) / 255.0  # Normalize to [0, 1]

#     # Make predictions
#     predictions = model.predict(img_array)
#     predicted_class_idx = np.argmax(predictions, axis=-1)[0]

#     # Handle unknown prediction
#     confidence = predictions[0][predicted_class_idx]
#     if confidence < threshold:
#         return "Unknown"
#     return class_names[predicted_class_idx]

# # Example usage

new_model = tf.keras.models.load_model("my_model.keras")

# print([layer.name for layer in new_model.layers])


# image_url = "https://staticdelivery.nexusmods.com/mods/2763/images/312/312-1558896521-2067846259.jpeg"
# threshold = 0.5  # Adjust based on your model's confidence levels
# predicted_label = predict_with_unknown(new_model, image_url, disease_types, threshold)
# print("Predicted Label:", predicted_label)
# print(threshold)

import os
print(os.path.exists("my_model.keras"))  # Ensure file exists
print(os.access("my_model.keras", os.R_OK))  # Ensure file is readable
