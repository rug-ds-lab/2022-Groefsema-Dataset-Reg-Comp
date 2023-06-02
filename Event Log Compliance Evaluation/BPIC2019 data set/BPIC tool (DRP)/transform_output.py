import os

PATH = "./log"

for filename in os.listdir(PATH):
    file_path = os.path.join(PATH, filename)
    
    if os.path.isfile(file_path) and filename.startswith("log_precedence"):
        f = open(file_path, "r")
        lines = f.readlines()
        transformed = map(
            lambda l: "[\"{}\"]".format(l.replace(",N/A,N/A,N/A,N/A", "").strip()), 
            lines
        )
        output = "[{}]".format(",".join(transformed))
        output_file_path = os.path.join(PATH, "trans_" + filename)
        output_file = open(output_file_path, 'w')
        output_file.write(output)
        output_file.close()

        
        
