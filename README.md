<div align="center">

  # 📝 TaskFlow

  **Um gerenciador de tarefas moderno, fluído e reativo para Android.**

  [![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
  [![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-Material_3-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
  [![Room Database](https://img.shields.io/badge/Room-SQLite-003B57?style=for-the-badge&logo=sqlite&logoColor=white)](https://developer.android.com/training/data-storage/room)
  [![Android](https://img.shields.io/badge/Android-SDK_26%2B-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com)
  [![Architecture](https://img.shields.io/badge/Architecture-MVVM-FF6F00?style=for-the-badge)](https://developer.android.com/topic/architecture)

  <br />

  🎓 **Projeto de Conclusão de Curso**

</div>

---

## 📌 Sobre o Projeto

O **TaskFlow** é um aplicativo Android nativo desenvolvido com o objetivo de oferecer uma experiência simples, elegante e altamente eficiente no gerenciamento diário de tarefas. 

Construído utilizando as recomendações mais recentes do **Google e Android Jetpack**, o app utiliza **Jetpack Compose** para uma interface declarativa e responsiva, **Room Database** para armazenamento local offline-first e **Kotlin Coroutines / Flow** para reatividade em tempo real.

---

## ✨ Funcionalidades

- ➕ **Criação de Tarefas**: Adicione rapidamente descrições e metas diárias.
- 📅 **Seleção de Data Limite**: Integrado com o `DatePicker` oficial do **Material 3**.
- ⚡ **Reatividade em Tempo Real**: A lista de tarefas se atualiza instantaneamente a cada inserção ou exclusão.
- 📦 **Persistência de Dados Local (Offline-First)**: Dados salvos com segurança utilizando **Room Database**.
- 🎨 **Design System Customizado**: Cores e tipografia configuradas via **Material Theme Builder** com suporte a *Edge-to-Edge*.
- ✅ **Confirmação de Conclusão**: Diálogo interativo para confirmar a realização e remoção de tarefas.

---

## 🏗️ Arquitetura & Tecnologias

O projeto segue estritamente a **Arquitetura MVVM (Model-View-ViewModel)** recomendada pelo Google, garantindo separação de responsabilidades, testabilidade e fácil manutenção.

```
┌─────────────────────────────────────────────────────────────┐
│                    UI Layer (Jetpack Compose)               │
│      [TaskManagerLayout]  ◄───►  [TaskManagerContent]       │
└──────────────────────────────┬──────────────────────────────┘
                               │ Observa StateFlow
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                     ViewModel Layer                         │
│                     [TaskViewModel]                         │
└──────────────────────────────┬──────────────────────────────┘
                               │ Executa Corrotinas / Flow
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                   Data Layer (Room / DAO)                   │
│         [TaskDao]  ◄───►  [AppDatabase (SQLite)]            │
└──────────────────────────────┬──────────────────────────────┘
```

### 🧰 Tech Stack:
- **Linguagem**: [Kotlin](https://kotlinlang.org/)
- **UI Toolkit**: [Jetpack Compose](https://developer.android.com/jetpack/compose) & [Material 3](https://m3.material.io/)
- **Gerenciamento de Estado**: `ViewModel`, `StateFlow` & `collectAsState`
- **Banco de Dados Local**: [Room Persistence Library](https://developer.android.com/training/data-storage/room)
- **Programação Assíncrona**: Kotlin Coroutines & `Flow`
- **Design System**: Material Theme Builder

---

## 🚀 Como Executar o Projeto

### Pré-requisitos
- **Android Studio** (versão Ladybug ou superior recomendada).
- **JDK 17** ou superior.
- Dispositivo físico ou Emulador Android rodando **Android 8.0 (API 26)** ou superior.

### Passo a Passo

1. **Clone o repositório:**
   ```bash
   git clone https://github.com/seu-usuario/TaskFlow.git
   ```

2. **Abra o projeto:**
   Abra o Android Studio e selecione a opção **Open**, navegando até a pasta do projeto clonado.

3. **Sincronize o Gradle:**
   Aguarde o Android Studio baixar as dependências e realizar a sincronização automática (`Gradle Sync`).

4. **Execute o aplicativo:**
   Selecione o seu dispositivo ou emulador e clique no botão **Run (Shift + F10)**.

---

## 📄 Licença

Este projeto foi desenvolvido para fins educacionais como trabalho de conclusão de curso. Sinta-se à vontade para estudar, utilizar ou contribuir com melhorias!

---

<div align="center">
  <sub>Desenvolvido com ❤️ e Kotlin.</sub>
</div>
