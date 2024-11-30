# # import os

# # os.environ['TF_ENABLE_ONEDNN_OPTS'] = '0'
# # os.environ['KERAS_BACKEND'] = 'tensorflow'

# # import os
# # os.environ["CUDA_VISIBLE_DEVICES"] = ""

# import tensorflow as tf
# import keras
# import numpy as np


# # print(keras.__version__)
# # print(tf.__version__)

# disease_types = [
#     'Apple___Apple_scab', 'Apple___Black_rot', 'Apple___Cedar_apple_rust', 'Apple___healthy',
#     'Cherry___Powdery_mildew', 'Cherry___healthy',
#     'Peach___Bacterial_spot', 'Peach___healthy', 'Pepper___bell___Bacterial_spot', 
#     'Pepper___bell___healthy', 'Potato___Early_blight', 'Potato___Late_blight', 
#     'Potato___healthy', 'Strawberry___Leaf_scorch', 'Strawberry___healthy',
#     'Tomato___Bacterial_spot', 'Tomato___Early_blight', 'Tomato___Late_blight',
#     'Tomato___Leaf_Mold', 'Tomato___Septoria_leaf_spot', 
#     'Tomato___Spider_mites_Two-spotted_spider_mite', 'Tomato___Target_Spot', 
#     'Tomato___Tomato_Yellow_Leaf_Curl_Virus', 'Tomato___Tomato_mosaic_virus', 
#     'Tomato___healthy'
# ]



# # test_images = "C:/Users/Allan/Pictures/Saved Pictures/adachi.jpg"

# # new_model = tf.keras.models.load_model('my_model.keras')

# # # Show the model architecture
# # new_model.summary()

# # # Evaluate the restored model


# # print(new_model.predict(test_images).shape)

# from PIL import Image
# from io import BytesIO

# def predict_tf(self, image, save_dir='saved_images'):
#     class_names = [
#         'Apple___Apple_scab', 'Apple___Black_rot', 'Apple___Cedar_apple_rust', 'Apple___healthy',
#         'Cherry___Powdery_mildew', 'Cherry___healthy',
#         'Peach___Bacterial_spot', 'Peach___healthy', 'Pepper___bell___Bacterial_spot', 
#         'Pepper___bell___healthy', 'Potato___Early_blight', 'Potato___Late_blight', 
#         'Potato___healthy', 'Strawberry___Leaf_scorch', 'Strawberry___healthy',
#         'Tomato___Bacterial_spot', 'Tomato___Early_blight', 'Tomato___Late_blight',
#         'Tomato___Leaf_Mold', 'Tomato___Septoria_leaf_spot', 
#         'Tomato___Spider_mites_Two-spotted_spider_mite', 'Tomato___Target_Spot', 
#         'Tomato___Tomato_Yellow_Leaf_Curl_Virus', 'Tomato___Tomato_mosaic_virus', 
#         'Tomato___healthy'
#     ]
    
#     # Decode the base64 string to get image data
#     img = Image.open(BytesIO(image))
    
#     # Preprocess the image
#     img_resize = img.resize((224, 224))
#     img_array = np.array(img_resize)
#     img_array = img_array.astype(np.float32) / 255.0
    
#     # Predict the class of the image
#     prediction = self.loaded_model(np.expand_dims(img_array, axis=0))
    
#     # Convert prediction to numpy array if it's a tensor
#     if isinstance(prediction, tf.Tensor):
#         prediction = prediction.numpy()
    
#     # Get confidence scores
#     confidence_scores = prediction[0]
#     max_confidence = np.max(confidence_scores)
#     predicted_class_index = np.argmax(confidence_scores)
    
#     # Check if confidence is below threshold
#     if max_confidence < self.confidence_threshold:
#         predicted_class_name = "Unknown Disease"
#     else:
#         predicted_class_name = class_names[predicted_class_index]
    
#     # Return both class name and confidence
#     return {
#         "class_name": predicted_class_name,
#         "confidence": float(max_confidence)
#     }

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
#     print(confidence)
#     if confidence <= threshold:
#         return "Unknown"
#     return class_names[predicted_class_idx]

# # # Example usage

# # new_model = tf.keras.models.load_model("my_model.keras")
# # print([layer.name for layer in new_model.layers])

# new_model = tf.saved_model.load("C:/Users/Allan/Desktop/leafy/fastapi/model-fix")


# #image_url = "https://staticdelivery.nexusmods.com/mods/2763/images/312/312-1558896521-2067846259.jpeg"
# test_images = "picture/rikimaru.jpg"

# threshold = 0.5  # Adjust based on your model's confidence levels
# predicted_label = predict_tf(new_model, test_images)
# print("Predicted Label:", predicted_label)

import tensorflow as tf
import numpy as np
from PIL import Image
from io import BytesIO

class PlantDiseasePredictor:
    def __init__(self, model_path, confidence_threshold=0.5):
        self.loaded_model = tf.saved_model.load(model_path)
        self.confidence_threshold = confidence_threshold
        self.class_names = [
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

    def predict(self, image_path):
        # Load the image
        img = Image.open(image_path)
        
        # Preprocess the image
        img_resize = img.resize((224, 224))
        img_array = np.array(img_resize).astype(np.float32) / 255.0

        # Predict the class of the image
        prediction = self.loaded_model(np.expand_dims(img_array, axis=0))

        # Convert prediction to numpy array if it's a tensor
        if isinstance(prediction, tf.Tensor):
            prediction = prediction.numpy()
        
        # Get confidence scores
        confidence_scores = prediction[0]
        max_confidence = np.max(confidence_scores)
        predicted_class_index = np.argmax(confidence_scores)
        
        # Check if confidence is below threshold
        if max_confidence < self.confidence_threshold:
            predicted_class_name = "Unknown Disease"
        else:
            predicted_class_name = self.class_names[predicted_class_index]
        
        # Return both class name and confidence
        return {
            "class_name": predicted_class_name,
            "confidence": float(max_confidence)
        }

# Example Usage
model_path = "C:/Users/Allan/Desktop/leafy/fastapi/model-fix"
predictor = PlantDiseasePredictor(model_path, confidence_threshold=0.5)

test_image_path = "C:/Users/Allan/Pictures/Saved Pictures/Untitled.jpg"
predicted_label = predictor.predict(test_image_path)

print("Predicted Label:", predicted_label)
