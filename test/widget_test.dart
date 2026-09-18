import 'package:flutter_test/flutter_test.dart';

import 'package:auto_photo_whatsapp/main.dart';

void main() {
  testWidgets(
    'aplicativo inicia corretamente',
    (WidgetTester tester) async {
      await tester.pumpWidget(
        const AutoPhotoApp(
          consentAccepted: false,
          recipientName: 'Claudia',
          recipientPhone: '',
        ),
      );

      expect(
        find.text('Automação de fotos'),
        findsOneWidget,
      );

      expect(
        find.text('CONTINUAR'),
        findsOneWidget,
      );
    },
  );
}