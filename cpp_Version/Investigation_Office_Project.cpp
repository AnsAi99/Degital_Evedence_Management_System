#include <iostream>
#include <fstream>
#include <string>
#include <algorithm> // ·«” Œœ«„ œÊ«·  ‰ŸÌ› «·‰’Ê’

using namespace std;

// ==========================================
// 1. Classes for Nodes («·›∆«  «·Œ«’… »«·⁄ıﬁœ)
// ==========================================

// ﬂ·«” «·œ·Ì·: Ì„À· ⁄ﬁœ… (Node) œ«Œ· «·ﬁ«∆„… «·„ —«»ÿ… (Linked List)
class EvidenceNode {
public:
    int id; // «·„ı⁄—› «·Œ«’ »«·œ·Ì· (ÌıœŒ· ÌœÊÌ«)

    //  ›«’Ì· «·œ·Ì· «·—ﬁ„Ì
    string evidenceType;    // ‰Ê⁄ «·œ·Ì· (Â« ›° ﬁ—’ ’·»° ≈·Œ)
    string evidenceDetails; // Ê’› «·œ·Ì· ÊÕ«· Â ⁄‰œ «·÷»ÿ

    //  ›«’Ì· «·ﬁ÷Ì… «·„— »ÿ… »«·œ·Ì·
    string caseNumber;      // —ﬁ„ «·ﬁ÷Ì…
    string caseName;        // «”„ «·ﬁ÷Ì…
    string caseDetails;     // „·Œ’  ›«’Ì· «·ﬁ÷Ì…

    string status;          // Õ«·… «·œ·Ì· (Stored: „Œ“‰ / Processed:  „  „⁄«·Ã Â)

    EvidenceNode* next;     // „ƒ‘— ··⁄ﬁœ… «· «·Ì… ›Ì «·ﬁ«∆„… «·„ —«»ÿ…

    // Constructor (œ«·… «·»‰«¡ · ÂÌ∆… «·⁄ﬁœ…)
    EvidenceNode(int id, string eType, string eDetails, string cNum, string cName, string cDetails, string s) {
        this->id = id;
        this->evidenceType = eType;
        this->evidenceDetails = eDetails;
        this->caseNumber = cNum;
        this->caseName = cName;
        this->caseDetails = cDetails;
        this->status = s;
        this->next = NULL;
    }
};

// ﬂ·«” «·ÿ«»Ê—: Ì„À· ⁄ﬁœ… (Node) œ«Œ· «·ÿ«»Ê— ·„⁄«·Ã… «·√œ·… »«· — Ì»
class QueueNode {
public:
    int evidenceId; // ‰Õ›Ÿ «·‹ ID ›ﬁÿ ›Ì «·ÿ«»Ê— Ê·Ì” ﬂ· «·»Ì«‰« 
    QueueNode* next;

    QueueNode(int id) {
        this->evidenceId = id;
        this->next = NULL;
    }
};

// ==========================================
// 2. Queue Class (›∆… «·ÿ«»Ê— - FIFO)
// ==========================================
// Ì⁄„· »‰Ÿ«„ (First In First Out): √Ê· œ·Ì· ÌœŒ· «·‰Ÿ«„ ÂÊ √Ê· œ·Ì·   „ „⁄«·Ã Â
class EvidenceQueue {
private:
    QueueNode* front; // »œ«Ì… «·ÿ«»Ê—
    QueueNode* rear;  // ‰Â«Ì… «·ÿ«»Ê—

public:
    EvidenceQueue() {
        front = rear = NULL;
    }

    // Destructor:  ‰ŸÌ› «·ÿ«»Ê— „‰ «·–«ﬂ—… ⁄‰œ ≈€·«ﬁ «·»—‰«„Ã
    ~EvidenceQueue() {
        while (!isEmpty()) {
            dequeue();
        }
    }

    // ≈÷«›… œ·Ì· ≈·Ï ‰Â«Ì… «·ÿ«»Ê—
    void enqueue(int id) {
        QueueNode* newNode = new QueueNode(id);
        if (rear == NULL) {
            front = rear = newNode;
        }
        else {
            rear->next = newNode;
            rear = newNode;
        }
    }

    // ”Õ» (≈Œ—«Ã) œ·Ì· „‰ »œ«Ì… «·ÿ«»Ê— ·„⁄«·Ã Â
    int dequeue() {
        if (isEmpty()) return -1;

        QueueNode* temp = front;
        int id = temp->evidenceId;
        front = front->next;

        if (front == NULL) {
            rear = NULL;
        }

        delete temp; //  Õ—Ì— «·–«ﬂ—…
        return id;
    }

    // «· Õﬁﬁ „„« ≈–« ﬂ«‰ «·ÿ«»Ê— ›«—€«
    bool isEmpty() {
        return front == NULL;
    }
};

// ==========================================
// 3. Main Evidence Management System Class
// ==========================================
// Â–Â «·›∆…  œÌ— «·ﬁ«∆„… «·„ —«»ÿ… «·√”«”Ì… Ê Õ ÊÌ ⁄·Ï œÊ«· «·‰Ÿ«„ (≈÷«›…° Õ–›°  ⁄œÌ·)
class EvidenceSystem {
private:
    EvidenceNode* head; // „ƒ‘— »œ«Ì… ﬁ«∆„… «·√œ·…
    EvidenceNode* tail; // „ƒ‘— ‰Â«Ì… ﬁ«∆„… «·√œ·…
    EvidenceQueue processQueue; // ÿ«»Ê— «·„⁄«·Ã…

    // œ«·… „”«⁄œ… ·≈÷«›… ⁄ﬁœ… ›Ì ‰Â«Ì… «·ﬁ«∆„… «·„ —«»ÿ…
    void appendNode(EvidenceNode* newNode) {
        if (head == NULL) {
            head = tail = newNode;
        }
        else {
            tail->next = newNode;
            tail = newNode;
        }
    }

    // œ«·… „”«⁄œ… ·„‰⁄ «·„” Œœ„ „‰  Œ—Ì» «·„·› «·‰’Ì »ﬂ «»… «·—„“ |
    void cleanString(string& str) {
        replace(str.begin(), str.end(), '|', '-');
    }

public:
    // ⁄‰œ  ‘€Ì· «·‰Ÿ«„ Ì „  Õ„Ì· «·»Ì«‰«  «·„Õ›ÊŸ… ”«»ﬁ«
    EvidenceSystem() {
        head = tail = NULL;
        loadFromFile();
    }

    // ⁄‰œ ≈€·«ﬁ «·‰Ÿ«„ Ì „ Õ›Ÿ «· ⁄œÌ·«  ›Ì «·„·›
    ~EvidenceSystem() {
        saveToFile();
        EvidenceNode* temp = head;
        while (temp != NULL) {
            EvidenceNode* toDelete = temp;
            temp = temp->next;
            delete toDelete;
        }
    }

    // œ«·… ··»ÕÀ ⁄‰ œ·Ì· »‰«¡ ⁄·Ï «·‹ ID «·Œ«’ »Â
    EvidenceNode* searchEvidence(int id) {
        EvidenceNode* temp = head;
        while (temp) {
            if (temp->id == id)
                return temp;
            temp = temp->next;
        }
        return NULL;
    }

    // 1. œ«·… ≈÷«›… œ·Ì· ÃœÌœ ··ﬁ«∆„… Ê··ÿ«»Ê—
    void addEvidence() {
        int id;
        string eType, eDetails, cNum, cName, cDetails;

        cout << "\n--- Case Details  ---\n";
        cout << "Case Number: ";
        getline(cin, cNum);
        cleanString(cNum);

        cout << "Case Name: ";
        getline(cin, cName);
        cleanString(cName);

        cout << "Case Description: ";
        getline(cin, cDetails);
        cleanString(cDetails);

        cout << "\n--- Evidence Details---\n";
        cout << "Evidence ID (Number): ";

        // Õ„«Ì… «·‰Ÿ«„ ›Ì Õ«· √œŒ· «·„” Œœ„ Õ—Ê› »œ·« „‰ «·√—ﬁ«„
        if (!(cin >> id)) {
            cin.clear();
            cin.ignore(10000, '\n');
            cout << "=> Invalid ID! Must be a number.\n";
            return;
        }
        cin.ignore(10000, '\n');

        // «· Õﬁﬁ „‰ √‰ Â–« «·—ﬁ„ €Ì— „ÊÃÊœ „”»ﬁ«
        if (searchEvidence(id) != NULL) {
            cout << "=> Error: Evidence ID " << id << " already exists!\n";
            return;
        }

        cout << "Evidence Type (e.g., Hard Drive, Phone): ";
        getline(cin, eType);
        cleanString(eType);

        cout << "Evidence Description: ";
        getline(cin, eDetails);
        cleanString(eDetails);

        // ≈‰‘«¡ «·⁄ﬁœ… Ê≈÷«› Â«
        EvidenceNode* newEvidence = new EvidenceNode(id, eType, eDetails, cNum, cName, cDetails, "Stored");
        appendNode(newEvidence);

        // ≈÷«›… «·‹ ID ·ÿ«»Ê— «·„⁄«·Ã… ·√‰ «·œ·Ì· ÃœÌœ ÊÕ«· Â Stored
        processQueue.enqueue(newEvidence->id);

        cout << "\n=> Evidence Added successfully with ID: " << newEvidence->id << "\n";
    }

    // 2. œ«·… ⁄—÷ Ã„Ì⁄ «·√œ·… «·„ÊÃÊœ… ›Ì «·ﬁ«∆„… «·„ —«»ÿ…
    void displayEvidence() {
        if (!head) {
            cout << "=> No Data Available.\n";
            return;
        }

        EvidenceNode* temp = head;
        cout << "\n========== Digital Evidence Records ==========\n";
        while (temp) {
            cout << "Evidence ID   : " << temp->id << " [" << temp->status << "]\n";
            cout << "----------------------------------------------\n";
            cout << "[ Case Info ]\n";
            cout << "Case Number   : " << temp->caseNumber << "\n";
            cout << "Case Name     : " << temp->caseName << "\n";
            cout << "Case Details  : " << temp->caseDetails << "\n";
            cout << "\n[ Evidence Info ]\n";
            cout << "Evidence Type : " << temp->evidenceType << "\n";
            cout << "Evidence Desc : " << temp->evidenceDetails << "\n";
            cout << "==============================================\n";
            temp = temp->next;
        }
    }

    // 3. œ«·… · ⁄œÌ· »Ì«‰«  «·œ·Ì·
    void editEvidence() {
        int id;
        cout << "\nEnter Evidence ID to edit: ";
        if (!(cin >> id)) {
            cin.clear();
            cin.ignore(10000, '\n');
            cout << "=> Invalid Input!\n";
            return;
        }
        cin.ignore(10000, '\n');

        EvidenceNode* e = searchEvidence(id);
        if (!e) {
            cout << "=> Evidence Not Found!\n";
            return;
        }

        cout << "\n-- Press Enter to keep current value or type new value --\n";

        string input;

        //   ÌÕ ··„” Œœ„ «·«Õ ›«Ÿ »«·ﬁÌ„… «·ﬁœÌ„… »÷€ÿ “— Enter ›ﬁÿ
        cout << "Case Name (" << e->caseName << "): ";
        getline(cin, input);
        if (!input.empty()) { cleanString(input); e->caseName = input; }

        cout << "Case Details (" << e->caseDetails << "): ";
        getline(cin, input);
        if (!input.empty()) { cleanString(input); e->caseDetails = input; }

        cout << "Evidence Details (" << e->evidenceDetails << "): ";
        getline(cin, input);
        if (!input.empty()) { cleanString(input); e->evidenceDetails = input; }

        cout << "=> Evidence Updated successfully.\n";
    }

    // 4. œ«·… «·Õ–› „‰ «·ﬁ«∆„… «·„ —«»ÿ…
    void deleteEvidence() {
        int id;
        cout << "\nEnter Evidence ID to delete: ";
        if (!(cin >> id)) {
            cin.clear();
            cin.ignore(10000, '\n');
            cout << "=> Invalid Input!\n";
            return;
        }
        cin.ignore(10000, '\n');

        EvidenceNode* temp = head;
        EvidenceNode* prev = NULL;

        // «·»ÕÀ ⁄‰ «·⁄ﬁœ… «·„—«œ Õ–›Â« Ê«·«Õ ›«Ÿ »«·⁄ﬁœ… «· Ì  ”»ﬁÂ«
        while (temp && temp->id != id) {
            prev = temp;
            temp = temp->next;
        }

        if (!temp) {
            cout << "=> Evidence Not Found!\n";
            return;
        }

        // ≈–« ﬂ«‰ «·œ·Ì· ÂÊ «·⁄ﬁœ… «·√Ê·Ï (Head)
        if (!prev) {
            head = temp->next;
        }
        // ≈–« ﬂ«‰ «·œ·Ì· ›Ì «·„‰ ’› √Ê «·‰Â«Ì…
        else {
            prev->next = temp->next;
        }

        //  ÕœÌÀ „ƒ‘— «·‹ Tail ≈–« Õ–›‰« «·⁄ﬁœ… «·√ŒÌ—…
        if (temp == tail) {
            tail = prev;
        }

        delete temp; //  Õ—Ì— «·–«ﬂ—… (≈“«·… «·⁄ﬁœ… ›⁄·Ì«)
        cout << "=> Evidence Deleted successfully.\n";
    }

    // 5. œ«·… „⁄«·Ã… «·√œ·… »‰Ÿ«„ «·ÿ«»Ê— ( „ ≈’·«Õ Œÿ√ «· ŒÿÌ Â‰«)
    void processNextEvidence() {
        if (processQueue.isEmpty()) {
            cout << "=> Processing Queue is Empty.\n";
            return;
        }

        EvidenceNode* e = NULL;
        int id = -1;

        // «·Õ·ﬁ… „” „—… ·”Õ» «·√œ·…° ·√‰ «·œ·Ì· ﬁœ ÌﬂÊ‰ „Õ–Ê›« „‰ «·ﬁ«∆„… «·„ —«»ÿ… 
        // ·ﬂ‰ —ﬁ„Â (ID) ·« Ì“«· „Œ“‰« œ«Œ· «·ÿ«»Ê—.
        while (!processQueue.isEmpty()) {
            id = processQueue.dequeue(); // √Œ– «· «·Ì
            e = searchEvidence(id);      // Â· ·« Ì“«· „ÊÃÊœ« »«·‰Ÿ«„ø

            if (e != NULL) {
                break; // ÊÃœ‰« œ·Ì·« ’«·Õ« ··„⁄«·Ã…° ‰Êﬁ› «·»ÕÀ
            }
            // ≈–« ﬂ«‰ „Õ–Ê›« Ìÿ»⁄  ŒÿÌ ÊÌﬁÊ„ »”Õ» «·–Ì Ì·ÌÂ  ·ﬁ«∆Ì« »›÷· «·‹ while
            cout << "=> Evidence ID " << id << " was deleted. Auto-skipping to next...\n";
        }

        // ≈–« ›—€‰« «·ÿ«»Ê— Êﬂ·Â„ ﬂ«‰Ê« „Õ–Ê›Ì‰
        if (e == NULL) {
            cout << "=> No valid pending evidences left in the queue.\n";
            return;
        }

        e->status = "Processed";
        cout << "=> Processing Evidence ID: " << e->id << "\n";
        cout << "   Related to Case: " << e->caseName << "\n";
        cout << "   Status updated to Processed.\n";
    }

    // 6. Õ›Ÿ «·ﬁ«∆„… «·„ —«»ÿ… »«·ﬂ«„· ›Ì „·› ‰’Ì (File I/O)
    void saveToFile() {
        ofstream file("evidence.txt");
        if (!file) return;

        EvidenceNode* temp = head;
        while (temp) {
            // ‰” Œœ„ | ﬂ›«’· »Ì‰ «·√⁄„œ…
            file << temp->id << "|"
                << temp->evidenceType << "|"
                << temp->evidenceDetails << "|"
                << temp->caseNumber << "|"
                << temp->caseName << "|"
                << temp->caseDetails << "|"
                << temp->status << "\n";
            temp = temp->next;
        }

        file.close();
        cout << "=> Data saved to file successfully.\n";
    }

    // 7. «” —Ã«⁄ «·»Ì«‰«  „‰ «·„·› Ê≈⁄«œ… »‰«¡ «·ﬁ«∆„… Ê«·ÿ«»Ê— ( „ ≈’·«Õ Œÿ√ \r Â‰«)
    void loadFromFile() {
        ifstream file("evidence.txt");
        if (!file) return; // «·„·› €Ì— „ÊÃÊœ »⁄œ

        string idStr, eType, eDetails, cNum, cName, cDetails, status;

        while (getline(file, idStr, '|')) {
            if (idStr.empty() || idStr == "\n") continue;

            getline(file, eType, '|');
            getline(file, eDetails, '|');
            getline(file, cNum, '|');
            getline(file, cName, '|');
            getline(file, cDetails, '|');
            getline(file, status); //  ﬁ—√ Õ Ï ‰Â«Ì… «·”ÿ—

            //  ‰ŸÌ› Õ«·… «·œ·Ì· „‰ √Ì ›—«€«  √Ê „”«›«  «·”ÿÊ— („Â„ Ãœ« ·‰Ÿ«„ Windows)
            if (!status.empty() && status[status.length() - 1] == '\r') {
                status.erase(status.length() - 1); // Õ–› —„“ \r
            }

            try {
                int id = stoi(idStr);
                EvidenceNode* e = new EvidenceNode(id, eType, eDetails, cNum, cName, cDetails, status);
                appendNode(e);

                // ≈–« ﬂ«‰  Õ«· Â Stored ‰⁄ÌœÂ ·ÿ«»Ê— «·„⁄«·Ã…
                if (status == "Stored") {
                    processQueue.enqueue(id);
                }
            }
            catch (...) {
                continue; // ›Ì Õ«· ÕœÊÀ Œÿ√ √À‰«¡ «· ÕÊÌ· ·—ﬁ„ Ì Ã«Â· «·”ÿ— ÊÌﬂ„·
            }
        }
        file.close();
    }
};

// ==========================================
// 4. Application Logic Class (Ê«ÃÂ… «· ÿ»Ìﬁ)
// ==========================================
class Application {
private:
    EvidenceSystem system;

    // œ«·…  Õﬁﬁ »”Ìÿ… · ”ÃÌ· «·œŒÊ·
    bool login() {
        string user, pass;
        cout << "=== Login ===\n";
        cout << "Username: ";
        cin >> user;
        cout << "Password: ";
        cin >> pass;
        return (user == "admin" && pass == "1234");
    }

public:
    void run() {
        if (!login()) {
            cout << "=> Access Denied. Incorrect username or password.\n";
            return;
        }

        int choice;
        do {
            cout << "\n========================================\n";
            cout << "  Digital Evidence Management System\n";
            cout << "========================================\n";
            cout << "1. Add Evidence & Case Details\n";
            cout << "2. Delete Evidence\n";
            cout << "3. Edit Evidence\n";
            cout << "4. Search Evidence\n";
            cout << "5. Display All Evidence (Full Report)\n";
            cout << "6. Process Next Evidence (Queue)\n";
            cout << "7. Save to File\n";
            cout << "8. Exit\n";
            cout << "Choose an option: ";

            if (!(cin >> choice)) {
                cin.clear();
                cin.ignore(10000, '\n');
                choice = 0;
            }
            else {
                cin.ignore(10000, '\n');
            }

            switch (choice) {
            case 1: system.addEvidence(); break;
            case 2: system.deleteEvidence(); break;
            case 3: system.editEvidence(); break;
            case 4: {
                int id;
                cout << "\nEnter ID to search: ";
                if (!(cin >> id)) {
                    cin.clear();
                    cin.ignore(10000, '\n');
                    cout << "=> Invalid Input!\n";
                    break;
                }
                cin.ignore(10000, '\n');

                EvidenceNode* e = system.searchEvidence(id);
                if (e) {
                    cout << "=> Found Evidence:\n";
                    cout << "   - Evidence Type : " << e->evidenceType << "\n";
                    cout << "   - Case Name     : " << e->caseName << "\n";
                    cout << "   - Status        : " << e->status << "\n";
                }
                else {
                    cout << "=> Not Found\n";
                }
                break;
            }
            case 5: system.displayEvidence(); break;
            case 6: system.processNextEvidence(); break;
            case 7: system.saveToFile(); break;
            case 8: cout << "\n=> Exiting System. Goodbye!\n"; break;
            default: cout << "=> Invalid choice! Please try again.\n"; break;
            }
        } while (choice != 8);
    }
};

// ==========================================
// ‰ﬁÿ… «·»œ«Ì… (Main Function)
// ==========================================
int main() {
    Application app;
    app.run();
    return 0;
}