# 📱 Associação Camila — Aplicativo Institucional

Aplicativo Android nativo desenvolvido para otimizar a gestão de presença em oficinas, logística interna e comunicação da **Associação Camila**, organização não governamental sem fins lucrativos.

---

## 📌 Visão Geral & Propósito

O sistema foi concebido para eliminar processos manuais em papel, garantir integridade no controle de assiduidade de alunos e dependentes e gerenciar a utilização de recursos institucionais (como o agendamento da van), priorizando:
- **Custo Zero de Infraestrutura:** Operação estruturada no plano gratuito (*Spark*) do Firebase Firestore, eliminando custos de faturamento e manutenção pesada de servidores.
- **Usabilidade e Acessibilidade:** Interface moderna baseada em Material Design 3, rápida para uso em campo e salas de aula.
- **Privacidade e Leveza:** Ausência de armazenamento massivo de mídias desnecessárias, utilizando identificação visual nativa via avatares tipográficos padronizados.

---

## ✨ Funcionalidades Principais

### 📋 Gestão de Frequência & Oficinas
- **Chamada Digital Interativa:** Registro de presenças e faltas por aluno com seleção rápida.
- **Histórico com Bloqueio de Edição:** Navegação temporal de registros com modo de apenas leitura (*read-only*) para dias anteriores, prevenindo alterações retroativas indevidas.
- **Exportação de Relatórios em PDF:** Extração de documentos formais com filtro por intervalo de datas customizado (`DateRangePicker`), contendo cálculo automático de métricas de presença.

### 🚐 Agendamento da Van Institucional
- **Módulo Logístico Restrito:** Disponível exclusivamente para perfis administrativos e docentes.
- **Prevenção Ativa de Conflitos:** Validação temporal algorítmica no Firestore, impedindo sobreposição de reservas no mesmo horário e data.
- **Visualização Diária:** Listagem organizada de itinerários, destinos e condutores responsáveis.

### 💬 Atendimento, Ajuda & Localização
- **Ações Rápidas de Contato:** Acesso direto via Intents nativas para conversa no WhatsApp institucional, discagem telefônica comercial e envio de e-mail.
- **Integração com Google Maps:** Rota imediata para a sede da organização.
- **Central de Dúvidas (FAQ):** Respostas expansíveis para as principais dúvidas sobre presença, oficinas e atestados.

### 🔐 Autenticação & Sessão
- **Sessão Persistente:** Auto-login mantido via Firebase Authentication até ação explícita de encerramento de sessão (*Sign Out*).
- **Controle de Acesso Baseado em Perfis (RBAC):** Restrições dinâmicas de interface e permissões de dados (Admin, Professor, Responsável/Aluno).

### 📢 Mural de Avisos Comunitários
- **Comunicação em Tempo Real:** Publicação de comunicados, recessos e eventos diretamente na Home.
- **Categorização Visual:** Sinalização por etiquetas temáticas (Geral, Recesso, Evento e Urgente).
- **Gestão Simplificada:** Criação e exclusão restritas à coordenação e docentes.

### 🤝 Programa de Voluntariado & Captação
- **Inscrição Pública:** Formulário integrado acessível na tela de login, dispensando formulários externos do Google Forms.
- **Mapeamento de Competências:** Coleta de dados com seleção de áreas de atuação (apoio operacional, oficinas, eventos, bazar e alimentação) e turnos disponíveis.
- **Painel Administrativo:** Acompanhamento de candidatos com triagem de status e contato direto via ligação ou WhatsApp.

---

## 🛠️ Tecnologias Utilizadas

- **Linguagem:** Kotlin
- **Interface Declarativa:** Jetpack Compose & Material 3
- **Navegação:** Jetpack Compose Navigation
- **Carregamento de Imagens & Vetores:** Coil Compose
- **Backend as a Service:** Firebase Authentication & Cloud Firestore
- **Geração de Documentos:** Android Graphics Canvas & `PdfDocument` nativo
- **Arquitetura:** MVVM (Model-View-ViewModel) orientada a fluxos reativos (`StateFlow` / `Flow`) e Clean Repository Pattern

---

## 🚀 Como Executar o Projeto

### Pré-requisitos
- Android Studio Ladybug ou superior
- JDK 17+
- Dispositivo Android físico ou Emulador com API 26+ (Android 8.0+)

### Passo a Passo
1. Clone o repositório:
   ```bash
   git clone [https://github.com/SEU-USUARIO/SEU-REPOSITORIO.git](https://github.com/SEU-USUARIO/SEU-REPOSITORIO.git)
