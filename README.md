# 🔷 Tesseract Client

> **Client de Minecraft 1.8.9** — desenvolvido do zero por [ThiagoKleinn](https://github.com/ThiagoKleinn).

---

## 📖 Sobre

O **Tesseract** é um client de Minecraft para a versão **1.8.9** construído sobre o **Minecraft Forge**, focado em melhorar a experiência de jogo legit. O projeto é desenvolvido em Java com Gradle como sistema de build.

---

## ⚙️ Tecnologias

| Tecnologia | Versão |
|---|---|
| Minecraft | 1.8.9 |
| Minecraft Forge | 11.15.1.2318-1.8.9 |
| Java | 8 (1.8) |
| Build System | Gradle + ForgeGradle 2.1 |
| MCP Mappings | stable_22 |

---

## 🚀 Como compilar

### Pré-requisitos

- **Java 8 (JDK)** instalado e configurado no PATH
- **Git** para clonar o repositório

### Passos

```bash
# 1. Clone o repositório
git clone https://github.com/ThiagoKleinn/tesseract.git
cd tesseract

# 2. Configure o ambiente Forge (necessário apenas na primeira vez)
./gradlew setupDecompWorkspace

# 3. Compile o projeto
./gradlew build
```

> No Windows, use `gradlew.bat` no lugar de `./gradlew`.

O `.jar` compilado será gerado em `build/libs/Tesseract-1.0.0.jar`.

---

## 🖥️ Executar em ambiente de desenvolvimento

```bash
# Inicia o cliente Minecraft com o mod carregado
./gradlew runClient
```

---

## 📁 Estrutura do projeto

```
tesseract/
├── src/
│   └── main/
│       ├── java/          # Código-fonte Java
│       └── resources/     # Recursos (mcmod.info, assets, etc.)
├── build.gradle           # Configuração do build
├── gradle.properties      # Propriedades do Gradle
└── gradlew / gradlew.bat  # Wrapper do Gradle
```

---

## 🤝 Contribuindo

Contribuições são bem-vindas! Sinta-se à vontade para abrir uma _issue_ ou enviar um _pull request_.

1. Faça um fork do repositório
2. Crie uma branch para sua feature (`git checkout -b feature/minha-feature`)
3. Faça commit das suas alterações (`git commit -m 'feat: adiciona minha feature'`)
4. Envie para o seu fork (`git push origin feature/minha-feature`)
5. Abra um Pull Request

---

## 📄 Licença

Este projeto está licenciado sob a licença **MIT** — veja o arquivo [LICENSE](LICENSE) para mais detalhes.

---

## 👤 Autor

Desenvolvido por **ThiagoKleinn**

- GitHub: [@ThiagoKleinn](https://github.com/ThiagoKleinn)