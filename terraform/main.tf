resource "aws_instance" "backend_staging" {
  ami           = "ami-0b016c703b95ecbe4"
  instance_type = "t3.small"
  subnet_id     = "subnet-08a085ab1cad92c17"
  vpc_security_group_ids = ["sg-0999acb66fb02e031"]
  key_name      = "llave"

  tags = {
    Name = "springboot-backend"
  }

  root_block_device {
    volume_size = 20
    volume_type = "gp3"
  }
}
