import tensorflow as tf
from PIL import Image
import numpy as np
import google.generativeai as genai
import base64
import os
from io import BytesIO
from PIL import Image
import time

class plant_disease_model:
    def __init__(self, model_path, confidence_threshold=0.5):
        self.loaded_model = tf.saved_model.load(model_path)
        self.confidence_threshold = confidence_threshold


    def predict_tf(self, base64_str, save_dir='saved_images'):
        class_names = [
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
        
        # Decode the base64 string to get image data
        img_data = base64.b64decode(base64_str)
        img = Image.open(BytesIO(img_data))
        
        # Preprocess the image
        img_resize = img.resize((224, 224))
        img_array = np.array(img_resize)
        img_array = img_array.astype(np.float32) / 255.0
        
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
            image_name = f"Unknown_{int(time.time())}.jpg"
        else:
            predicted_class_name = class_names[predicted_class_index]
            image_name = f"{predicted_class_name}_{int(time.time())}.jpg"
        
        # Save the image
        if not os.path.exists(save_dir):
            os.makedirs(save_dir)
        save_path = os.path.join(save_dir, image_name)
        img.save(save_path)
        
        # Return both class name and confidence
        return {
            "class_name": predicted_class_name,
            "confidence": float(max_confidence)
        }

    def prompt_unknown(disease):
        genai.configure(api_key="AIzaSyAdL316Q_O8FF7apV9VVl97Ue8iB6cHFXw")
        model = genai.GenerativeModel('gemini-pro')
        
        prompt = f"buat kalimat bahwa foto itu bukan daun tanaman."
        
        response = model.generate_content(prompt)
        return response.text

    def prompt_disease(disease):
        genai.configure(api_key="AIzaSyAdL316Q_O8FF7apV9VVl97Ue8iB6cHFXw")
        model = genai.GenerativeModel('gemini-pro')
        
        prompt = f"""
        ### 
        Jelaskan Penyakit {disease}: Pengertian, Penyebab, dan Cara Penanganan singkat dalam 3 paragraf. 
        Buka dengan menyebutkan jenis penyakit dalam bentuk baku dan dalam bentuk scientificnya. dalam format Markdown ya. Bagian Penanganan pakai List ya.


        Contoh format:
        # Penyakit {disease}: Pengertian, Penyebab, dan Cara Penanganan

        ---

        Penyakit {disease} (dalam bahasa ilmiah: scientific_name) adalah suatu kondisi medis yang ditandai dengan gangguan pada fungsi atau struktur tubuh, yang dapat mengarah pada gejala tertentu. Penyakit ini dapat mempengaruhi berbagai sistem dalam tubuh manusia dan dapat bervariasi dalam tingkat keparahan. Biasanya, {disease} mempengaruhi organ_system, menyebabkan main_symptom.

        Penyebab dari penyakit {disease} dapat beragam, termasuk faktor genetik, infeksi, atau faktor lingkungan. Infeksi bakteri atau virus sering kali menjadi penyebab utama, namun faktor gaya hidup seperti pola makan yang buruk, kurangnya aktivitas fisik, atau paparan terhadap bahan kimia berbahaya juga dapat memicu timbulnya penyakit ini. Beberapa kasus mungkin juga disebabkan oleh kelainan genetik yang mengganggu mekanisme tubuh dalam menjaga keseimbangan fungsional.

        ### Cara Penanganan

        Penanganan penyakit {disease} dapat dilakukan dengan berbagai cara, antara lain:

        - **Pengobatan medis**: Menggunakan obat-obatan yang sesuai untuk mengatasi gejala atau penyebab penyakit.
        - **Perubahan gaya hidup**: Menjaga pola makan sehat, berolahraga secara rutin, dan menghindari faktor risiko.
        - **Tindakan bedah**: Pada beberapa kasus yang lebih parah, prosedur bedah mungkin diperlukan untuk mengatasi kerusakan atau gangguan yang terjadi.
        - **Pencegahan**: Vaksinasi dan edukasi tentang cara menghindari faktor risiko adalah langkah penting untuk mencegah penyebaran penyakit ini.
        """
        
        response = model.generate_content(prompt)
        return response.text


    def prompt_healthy(plant_type):
        genai.configure(api_key="AIzaSyAdL316Q_O8FF7apV9VVl97Ue8iB6cHFXw")
        model = genai.GenerativeModel('gemini-pro')
        prompt = f"""Tanaman {plant_type} Anda sehat! Berikan saran perawatan rutin agar tetap sehat dan subur dalam 3 paragraf. 
        Buka dengan menyatakan bahwa jenis_tanamannya sehat. dalam format Markdown ya.
        
        Dengan contoh formatting seperti berikut:
        
        # **Tanaman {plant_type} [rubah namanya dalam bahasa baku] atau dikenal sebagai (nama saintifik)  blablablablabla!** 

        paragraf pembuka Blablablablablabla. Berikut adalah beberapa tips yang perlu diperhatikan:

        1. **blablablabla** 

        - blablablablabla. 
        - dst.

        2. **dst..**
        
        - dst

        ---

        paragraf penutup"""
        
        response = model.generate_content(prompt)
        return response.text
    
    def split_class_name(self, class_name):
        # Split the string by '___'
        plant, disease = class_name.split("___")
        
        # Remove underscores from the disease part
        disease = disease.replace("_", " ")
        
        # Capitalize the first letter of each word in the disease name
        disease_words = disease.split()
        capitalized_disease = ""
        for i, word in enumerate(disease_words):
            if i == 0:
                capitalized_disease += word.capitalize()
            else:
                capitalized_disease += " " + word.capitalize()

        # Check if the capitalized disease name has fewer than 12 characters
        if len(capitalized_disease) < 11:
            # Calculate how many spaces to add
            spaces_to_add = 11 - len(capitalized_disease)
            # Add invisible spaces (regular spaces in this case)
            capitalized_disease += " " * spaces_to_add
        
        # Example: Get the URL for a specific disease
        disease_url_mapping = {
            'Apple___Apple_scab': 'https://extension.umn.edu/plant-diseases/apple-scab',
            'Apple___Black_rot': 'https://extension.umn.edu/plant-diseases/black-rot-apple',
            'Apple___Cedar_apple_rust': 'https://extension.umn.edu/plant-diseases/cedar-apple-rust',
            'Apple___healthy': 'https://extension.umn.edu/fruit/growing-apples',  # General apple health guide
            'Cherry___Powdery_mildew': 'https://www.bctfpg.ca/pest_guide/info/101/',
            'Cherry___healthy': 'https://extension.umn.edu/fruit/growing-stone-fruits-home-garden',  # General cherry health guide
            'Peach___Bacterial_spot': 'https://www.aces.edu/blog/topics/crop-production/bacterial-spot-treatment-in-peaches/',
            'Peach___healthy': 'https://extension.umn.edu/fruit/growing-stone-fruits-home-garden',
            'Pepper___bell___Bacterial_spot': 'https://extension.umn.edu/disease-management/bacterial-spot-tomato-and-pepper',
            'Pepper___bell___healthy': 'https://extension.umn.edu/vegetables/growing-peppers',  # General guide
            'Potato___Early_blight': 'https://extension.umn.edu/disease-management/early-blight-tomato-and-potato',
            'Potato___Late_blight': 'https://extension.umn.edu/disease-management/late-blight',
            'Potato___healthy': 'https://extension.umn.edu/vegetables/growing-potatoes',  # General guide
            'Strawberry___Leaf_scorch': 'https://extension.umn.edu/fruit/growing-strawberries-home-garden#gray-mold%2C-leaf-blight%2C-leaf-scorch-and-leaf-spot--1008160',
            'Strawberry___healthy': 'https://extension.umn.edu/fruit/growing-strawberries-home-garden',  # General guide
            'Tomato___Bacterial_spot': 'https://extension.umn.edu/disease-management/bacterial-spot-tomato-and-pepper',
            'Tomato___Early_blight': 'https://extension.umn.edu/disease-management/early-blight-tomato-and-potato',
            'Tomato___Late_blight': 'https://extension.umn.edu/disease-management/late-blight',
            'Tomato___Leaf_Mold': 'https://extension.umn.edu/disease-management/tomato-leaf-mold',
            'Tomato___Septoria_leaf_spot': 'https://content.ces.ncsu.edu/septoria-leaf-spot-of-tomato',
            'Tomato___Spider_mites_Two-spotted_spider_mite': 'https://ag.umass.edu/vegetable/fact-sheets/two-spotted-spider-mite',
            'Tomato___Target_Spot': 'https://www.vegetables.bayer.com/ca/en-ca/resources/agronomic-spotlights/target-spot-of-tomato.html',
            'Tomato___Tomato_Yellow_Leaf_Curl_Virus': 'https://agriculture.vic.gov.au/biosecurity/plant-diseases/vegetable-diseases/tomato-yellow-leaf-curl-virus',
            'Tomato___Tomato_mosaic_virus': 'https://blogs.ifas.ufl.edu/stlucieco/2023/03/03/tomato-mosaic-virus-tomv-and-its-management/',
            'Tomato___healthy': 'https://extension.umn.edu/vegetables/growing-tomatoes',  # General guide
        }

        disease_url = disease_url_mapping.get(class_name, 'URL not found')
        print(f"The URL for {class_name} is {disease_url}")

        return plant, capitalized_disease, disease_url

    def main_tf(self, image_path):
        # Load the image and predict the class
        predicted_class_name = self.predict_tf(image_path)

        # Print the predicted class name
        print(self.prompt_disease(predicted_class_name))

