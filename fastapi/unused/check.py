# import tensorflow as tf

# # Load the TFLite model
# tflite_model_path = "fastapi\plant_model-fix.tflite"  # Replace with your TFLite model path

# # Load the TFLite model and interpret it
# interpreter = tf.lite.Interpreter(model_path=tflite_model_path)
# interpreter.allocate_tensors()

# # Get details about the input and output tensors
# input_details = interpreter.get_input_details()
# output_details = interpreter.get_output_details()

# print("Input details:", input_details)
# print("Output details:", output_details)

# prompt: add first letter capital for the disease after underscore replacer

# def split_class_name(class_name):
#     # Split the string by '___'
#     plant, disease = class_name.split("___")
    
#     # Remove underscores from the disease part
#     disease = disease.replace("_", " ")
    
#     # Capitalize the first letter of each word in the disease name
#     disease_words = disease.split()
#     capitalized_disease = ""
#     for i, word in enumerate(disease_words):
#         if i == 0:
#           capitalized_disease += word.capitalize()
#         else:
#           capitalized_disease += " " + word.capitalize()
    
#     return plant, capitalized_disease

# print(split_class_name("Tomato___Tomato_Yellow_Leaf_Curl_Virus"))

# Define a mapping of class names to URLs
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

# Example: Get the URL for a specific disease
selected_disease = 'Apple___Black_rot'
disease_url = disease_url_mapping.get(selected_disease, 'URL not found')
print(f"The URL for {selected_disease} is {disease_url}")
